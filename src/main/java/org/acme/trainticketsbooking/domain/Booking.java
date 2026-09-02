package org.acme.trainticketsbooking.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Booking(
        UUID id,
        BookingStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime cancelledAt
) {
}
