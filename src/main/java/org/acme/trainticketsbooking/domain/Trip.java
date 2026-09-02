package org.acme.trainticketsbooking.domain;

import java.time.OffsetDateTime;

public record Trip(
        Long id,
        Long trainId,
        OffsetDateTime departureTime,
        OffsetDateTime arrivalTime
) {
}
