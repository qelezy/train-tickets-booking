package org.acme.trainticketsbooking.dto.response;

import org.acme.trainticketsbooking.domain.booking.BookingStatus;
import org.acme.trainticketsbooking.domain.ticket.TicketStatus;

import java.util.UUID;

public record TicketCancelResponse(
        UUID ticketId,
        TicketStatus status,
        UUID bookingId,
        BookingStatus bookingStatus
) {
}
