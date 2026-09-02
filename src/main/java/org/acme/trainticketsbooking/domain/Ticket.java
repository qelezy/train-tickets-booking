package org.acme.trainticketsbooking.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Ticket(
        UUID id,
        UUID bookingId,
        Long tripCarriageId,
        short seatNumber,
        TicketStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime cancelledAt
) {
}
