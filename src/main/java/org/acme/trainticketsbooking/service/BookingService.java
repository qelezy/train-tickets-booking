package org.acme.trainticketsbooking.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.acme.trainticketsbooking.domain.BookingForCancellation;
import org.acme.trainticketsbooking.domain.BookingStatus;
import org.acme.trainticketsbooking.domain.CancelledBooking;
import org.acme.trainticketsbooking.domain.CancelledTicket;
import org.acme.trainticketsbooking.domain.CreatedBooking;
import org.acme.trainticketsbooking.domain.CreatedTicket;
import org.acme.trainticketsbooking.domain.OccupiedSeat;
import org.acme.trainticketsbooking.domain.SeatSelection;
import org.acme.trainticketsbooking.domain.TicketForCancellation;
import org.acme.trainticketsbooking.domain.TicketStatus;
import org.acme.trainticketsbooking.domain.TripCarriageSeatInfo;
import org.acme.trainticketsbooking.dto.request.BookingCreateRequest;
import org.acme.trainticketsbooking.dto.request.SeatRequest;
import org.acme.trainticketsbooking.dto.response.BookingCancelResponse;
import org.acme.trainticketsbooking.dto.response.BookingCreateResponse;
import org.acme.trainticketsbooking.dto.response.TicketCancelResponse;
import org.acme.trainticketsbooking.exception.BookingAlreadyCancelledException;
import org.acme.trainticketsbooking.exception.BookingNotFoundException;
import org.acme.trainticketsbooking.exception.CancellationTooLateException;
import org.acme.trainticketsbooking.exception.InvalidBookingException;
import org.acme.trainticketsbooking.exception.SeatAlreadyTakenException;
import org.acme.trainticketsbooking.exception.TicketAlreadyCancelledException;
import org.acme.trainticketsbooking.exception.TicketNotFoundException;
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

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;

    public BookingService(BookingRepository bookingRepository, BookingMapper bookingMapper) {
        this.bookingRepository = bookingRepository;
        this.bookingMapper = bookingMapper;
    }

    @Transactional
    public BookingCreateResponse createBooking(BookingCreateRequest request) {
        long tripId = request.tripId();
        List<TripCarriageSeatInfo> carriages = bookingRepository.lockTripWithCarriages(tripId)
            .orElseThrow(() -> new TripNotFoundException(tripId));

        List<SeatSelection> seats = resolveSeats(request.seats(), carriages);
        ensureSeatsAvailable(seats);

        UUID bookingId = bookingRepository.insertBooking();
        List<CreatedTicket> tickets = new ArrayList<>(seats.size());
        for (SeatSelection seat : seats) {
            tickets.add(bookingRepository.insertTicket(bookingId, seat));
        }

        return bookingMapper.toCreateResponse(new CreatedBooking(bookingId, tickets));
    }

    @Transactional
    public BookingCancelResponse cancelBooking(UUID bookingId) {
        BookingForCancellation booking = bookingRepository.lockBookingForCancellation(bookingId)
            .orElseThrow(() -> new BookingNotFoundException(bookingId));

        if (booking.status() == BookingStatus.CANCELLED) {
            throw new BookingAlreadyCancelledException(bookingId);
        }
        if (booking.departureTime() == null) {
            throw new InvalidBookingException("У бронирования отсутствует связанный рейс");
        }

        ensureCancellationAllowed(booking.departureTime(), booking.checkedAt());

        int updated = bookingRepository.cancelConfirmedBooking(bookingId);
        if (updated == 0) {
            throw new BookingAlreadyCancelledException(bookingId);
        }
        bookingRepository.cancelActiveTickets(bookingId);

        return bookingMapper.toCancelResponse(new CancelledBooking(bookingId, BookingStatus.CANCELLED));
    }

    @Transactional
    public TicketCancelResponse cancelTicket(UUID ticketId) {
        TicketForCancellation ticket = bookingRepository.lockTicketForCancellation(ticketId)
            .orElseThrow(() -> new TicketNotFoundException(ticketId));

        if (ticket.ticketStatus() == TicketStatus.CANCELLED) {
            throw new TicketAlreadyCancelledException(ticketId);
        }
        if (ticket.bookingStatus() == BookingStatus.CANCELLED) {
            throw new BookingAlreadyCancelledException(ticket.bookingId());
        }
        if (ticket.departureTime() == null) {
            throw new InvalidBookingException("У билета отсутствует связанный рейс");
        }

        ensureCancellationAllowed(ticket.departureTime(), ticket.checkedAt());

        int updated = bookingRepository.cancelActiveTicket(ticketId);
        if (updated == 0) {
            throw new TicketAlreadyCancelledException(ticketId);
        }

        int bookingUpdated = bookingRepository.cancelBookingIfNoActiveTickets(ticket.bookingId());
        BookingStatus bookingStatus = bookingUpdated > 0
            ? BookingStatus.CANCELLED
            : BookingStatus.CONFIRMED;

        return bookingMapper.toTicketCancelResponse(new CancelledTicket(
            ticketId,
            TicketStatus.CANCELLED,
            ticket.bookingId(),
            bookingStatus
        ));
    }

    private void ensureCancellationAllowed(OffsetDateTime departureTime, OffsetDateTime checkedAt) {
        OffsetDateTime cancelDeadline = departureTime.minusHours(2);
        if (checkedAt.isAfter(cancelDeadline)) {
            throw new CancellationTooLateException();
        }
    }

    private List<SeatSelection> resolveSeats(
            List<SeatRequest> seatRequests,
            List<TripCarriageSeatInfo> carriages
    ) {
        Map<Short, TripCarriageSeatInfo> carriageByNumber = new HashMap<>();
        for (TripCarriageSeatInfo carriage : carriages) {
            carriageByNumber.put(carriage.carriageNumber(), carriage);
        }

        List<SeatSelection> selections = new ArrayList<>(seatRequests.size());
        for (SeatRequest seat : seatRequests) {
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

    private void ensureSeatsAvailable(List<SeatSelection> seats) {
        Set<Long> tripCarriageIds = new HashSet<>();
        for (SeatSelection seat : seats) {
            tripCarriageIds.add(seat.tripCarriageId());
        }

        Set<OccupiedSeat> occupied = bookingRepository.findActiveSeats(tripCarriageIds);
        for (SeatSelection seat : seats) {
            OccupiedSeat key = new OccupiedSeat(seat.tripCarriageId(), seat.seatNumber());
            if (occupied.contains(key)) {
                throw new SeatAlreadyTakenException(seat.carriageNumber(), seat.seatNumber());
            }
        }
    }
}
