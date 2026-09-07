package org.acme.trainticketsbooking.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BookingForCancellation(
        UUID id,
        BookingStatus status,
        OffsetDateTime departureTime
) {
}
