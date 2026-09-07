package org.acme.trainticketsbooking.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.trainticketsbooking.domain.BookingForCancellation;
import org.acme.trainticketsbooking.domain.BookingStatus;
import org.acme.trainticketsbooking.domain.CancelledBooking;
import org.acme.trainticketsbooking.domain.CreatedBooking;
import org.acme.trainticketsbooking.domain.SeatSelection;
import org.acme.trainticketsbooking.domain.TripCarriageSeatInfo;
import org.acme.trainticketsbooking.dto.request.BookingCreateRequest;
import org.acme.trainticketsbooking.dto.request.SeatRequest;
import org.acme.trainticketsbooking.dto.response.BookingCancelResponse;
import org.acme.trainticketsbooking.dto.response.BookingCreateResponse;
import org.acme.trainticketsbooking.exception.BookingAlreadyCancelledException;
import org.acme.trainticketsbooking.exception.BookingNotFoundException;
import org.acme.trainticketsbooking.exception.CancellationTooLateException;
import org.acme.trainticketsbooking.exception.InvalidBookingException;
import org.acme.trainticketsbooking.exception.SeatAlreadyTakenException;
import org.acme.trainticketsbooking.exception.TripNotFoundException;
import org.acme.trainticketsbooking.mapper.BookingMapper;
import org.acme.trainticketsbooking.repository.BookingRepository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class BookingService {

    private static final int CANCEL_DEADLINE_HOURS = 2;

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;

    public BookingService(BookingRepository bookingRepository, BookingMapper bookingMapper) {
        this.bookingRepository = bookingRepository;
        this.bookingMapper = bookingMapper;
    }

    public BookingCreateResponse createBooking(BookingCreateRequest request) {
        long tripId = request.tripId();
        List<TripCarriageSeatInfo> carriages = bookingRepository.findTripCarriages(tripId)
            .orElseThrow(() -> new TripNotFoundException(tripId));

        Map<Short, TripCarriageSeatInfo> carriageByNumber = new HashMap<>();
        for (TripCarriageSeatInfo carriage : carriages) {
            carriageByNumber.put(carriage.carriageNumber(), carriage);
        }

        List<SeatSelection> selections = resolveSeats(request.seats(), carriageByNumber);
        ensureSeatsAvailable(selections);

        CreatedBooking created = bookingRepository.createBooking(selections);
        return bookingMapper.toCreateResponse(created);
    }

    public BookingCancelResponse cancelBooking(UUID bookingId) {
        BookingForCancellation booking = bookingRepository.findBookingForCancellation(bookingId)
            .orElseThrow(() -> new BookingNotFoundException(bookingId));

        if (booking.status() == BookingStatus.CANCELLED) {
            throw new BookingAlreadyCancelledException(bookingId);
        }
        if (booking.departureTime() == null) {
            throw new InvalidBookingException("У бронирования отсутствует связанный рейс");
        }
        if (!OffsetDateTime.now().isBefore(booking.departureTime().minusHours(CANCEL_DEADLINE_HOURS))) {
            throw new CancellationTooLateException();
        }

        CancelledBooking cancelled = bookingRepository.cancelBooking(bookingId);
        return bookingMapper.toCancelResponse(cancelled);
    }

    private List<SeatSelection> resolveSeats(
            List<SeatRequest> seats,
            Map<Short, TripCarriageSeatInfo> carriageByNumber
    ) {
        List<SeatSelection> selections = new ArrayList<>(seats.size());
        for (SeatRequest seat : seats) {
            short carriageNumber = seat.carriageNumber();
            short seatNumber = seat.seatNumber();

            TripCarriageSeatInfo carriage = carriageByNumber.get(carriageNumber);
            if (carriage == null) {
                throw new InvalidBookingException("Вагон " + carriageNumber + " не принадлежит указанному рейсу");
            }
            if (seatNumber < 1 || seatNumber > carriage.seatCount()) {
                throw new InvalidBookingException(
                    "Номер места " + seatNumber + " вне допустимого диапазона для вагона " + carriageNumber
                );
            }

            selections.add(new SeatSelection(
                carriage.tripCarriageId(),
                carriageNumber,
                seatNumber
            ));
        }
        return selections;
    }

    private void ensureSeatsAvailable(List<SeatSelection> selections) {
        Set<Long> tripCarriageIds = new HashSet<>();
        for (SeatSelection selection : selections) {
            tripCarriageIds.add(selection.tripCarriageId());
        }

        Set<BookingRepository.SeatKey> occupied = bookingRepository.findActiveSeats(tripCarriageIds);
        for (SeatSelection selection : selections) {
            BookingRepository.SeatKey key = new BookingRepository.SeatKey(
                selection.tripCarriageId(),
                selection.seatNumber()
            );
            if (occupied.contains(key)) {
                throw new SeatAlreadyTakenException(selection.carriageNumber(), selection.seatNumber());
            }
        }
    }
}
