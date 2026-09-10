package org.acme.trainticketsbooking.domain.trip;

public record TripCarriageSeatInfo(
        Long tripCarriageId,
        short carriageNumber,
        int seatCount
) {
}
