package org.acme.trainticketsbooking.resource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.trainticketsbooking.dto.request.BookingCreateRequest;
import org.acme.trainticketsbooking.dto.request.SeatRequest;
import org.acme.trainticketsbooking.service.BookingService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Path("/api/v1/bookings")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookingResource {

    private final BookingService bookingService;

    public BookingResource(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @POST
    public Response createBooking(BookingCreateRequest request) {
        validate(request);
        return Response.status(Response.Status.CREATED)
            .entity(bookingService.createBooking(request))
            .build();
    }

    @POST
    @Path("{bookingId}/cancel")
    public Response cancelBooking(@PathParam("bookingId") String bookingId) {
        return Response.ok(bookingService.cancelBooking(parseBookingId(bookingId))).build();
    }

    private UUID parseBookingId(String bookingId) {
        if (bookingId == null || bookingId.isBlank()) {
            throw new BadRequestException("Идентификатор бронирования обязателен");
        }
        try {
            return UUID.fromString(bookingId.trim());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Некорректный идентификатор бронирования");
        }
    }

    private void validate(BookingCreateRequest request) {
        if (request == null) {
            throw new BadRequestException("Тело запроса обязательно");
        }
        if (request.tripId() == null) {
            throw new BadRequestException("Идентификатор рейса обязателен");
        }
        if (request.tripId() <= 0) {
            throw new BadRequestException("Идентификатор рейса должен быть положительным");
        }

        List<SeatRequest> seats = request.seats();
        if (seats == null || seats.isEmpty()) {
            throw new BadRequestException("Список мест не может быть пустым");
        }

        Set<String> uniqueSeats = new HashSet<>();
        for (SeatRequest seat : seats) {
            if (seat == null) {
                throw new BadRequestException("Элемент списка мест не может быть пустым");
            }
            if (seat.carriageNumber() == null) {
                throw new BadRequestException("Номер вагона обязателен");
            }
            if (seat.seatNumber() == null) {
                throw new BadRequestException("Номер места обязателен");
            }
            if (seat.carriageNumber() <= 0) {
                throw new BadRequestException("Номер вагона должен быть положительным");
            }
            if (seat.seatNumber() <= 0) {
                throw new BadRequestException("Номер места должен быть положительным");
            }

            String key = seat.carriageNumber() + ":" + seat.seatNumber();
            if (!uniqueSeats.add(key)) {
                throw new BadRequestException(
                    "Дублирующееся место в запросе: вагон " + seat.carriageNumber()
                        + ", место " + seat.seatNumber()
                );
            }
        }
    }
}
