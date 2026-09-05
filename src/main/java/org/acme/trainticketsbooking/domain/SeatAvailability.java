package org.acme.trainticketsbooking.domain;

public record SeatAvailability(
        CarriageType carriageType,
        int availableSeatsCount
) {
}
