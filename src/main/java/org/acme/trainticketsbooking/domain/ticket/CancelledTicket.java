package org.acme.trainticketsbooking.domain.ticket;

import org.acme.trainticketsbooking.domain.booking.BookingStatus;

import java.util.UUID;

public record CancelledTicket(
        UUID ticketId,
        TicketStatus ticketStatus,
        UUID bookingId,
        BookingStatus bookingStatus
) {
}
