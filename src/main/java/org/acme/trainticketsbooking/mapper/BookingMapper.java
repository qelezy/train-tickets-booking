package org.acme.trainticketsbooking.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.trainticketsbooking.domain.CancelledBooking;
import org.acme.trainticketsbooking.domain.CancelledTicket;
import org.acme.trainticketsbooking.domain.CreatedBooking;
import org.acme.trainticketsbooking.domain.CreatedTicket;
import org.acme.trainticketsbooking.dto.response.BookingCancelResponse;
import org.acme.trainticketsbooking.dto.response.BookingCreateResponse;
import org.acme.trainticketsbooking.dto.response.TicketCancelResponse;
import org.acme.trainticketsbooking.dto.response.TicketResponse;

import java.util.List;

@ApplicationScoped
public class BookingMapper {

    public BookingCreateResponse toCreateResponse(CreatedBooking booking) {
        return new BookingCreateResponse(
            booking.bookingId(),
            toTicketResponseList(booking.tickets())
        );
    }

    public BookingCancelResponse toCancelResponse(CancelledBooking booking) {
        return new BookingCancelResponse(
            booking.bookingId(),
            booking.status()
        );
    }

    public TicketCancelResponse toTicketCancelResponse(CancelledTicket ticket) {
        return new TicketCancelResponse(
            ticket.ticketId(),
            ticket.ticketStatus(),
            ticket.bookingId(),
            ticket.bookingStatus()
        );
    }

    private List<TicketResponse> toTicketResponseList(List<CreatedTicket> tickets) {
        return tickets.stream().map(this::toTicketResponse).toList();
    }

    private TicketResponse toTicketResponse(CreatedTicket ticket) {
        return new TicketResponse(
            ticket.ticketId(),
            ticket.carriageNumber(),
            ticket.seatNumber(),
            ticket.status()
        );
    }
}
