package org.acme.trainticketsbooking.domain;

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
