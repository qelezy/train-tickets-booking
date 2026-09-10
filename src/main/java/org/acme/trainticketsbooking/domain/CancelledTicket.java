package org.acme.trainticketsbooking.domain;

import java.util.UUID;

public record CancelledTicket(
        UUID ticketId,
        TicketStatus ticketStatus,
        UUID bookingId,
        BookingStatus bookingStatus
) {
}
