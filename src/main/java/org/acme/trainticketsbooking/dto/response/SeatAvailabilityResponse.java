package org.acme.trainticketsbooking.dto.response;

import org.acme.trainticketsbooking.domain.trip.CarriageType;

public record SeatAvailabilityResponse(
        CarriageType carriageType,
        int availableSeatsCount
) {
}
