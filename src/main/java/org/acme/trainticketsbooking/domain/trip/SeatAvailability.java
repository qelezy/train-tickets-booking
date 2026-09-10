package org.acme.trainticketsbooking.domain.trip;

public record SeatAvailability(
        CarriageType carriageType,
        int availableSeatsCount
) {
}
