package org.acme.trainticketsbooking.domain.ticket;

import org.acme.trainticketsbooking.domain.booking.BookingStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TicketForCancellation(
        UUID ticketId,
        TicketStatus ticketStatus,
        UUID bookingId,
        BookingStatus bookingStatus,
        OffsetDateTime departureTime,
        OffsetDateTime checkedAt
) {
}
