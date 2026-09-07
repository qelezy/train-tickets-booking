package org.acme.trainticketsbooking.domain;

public record TripCarriageSeatInfo(
        Long tripCarriageId,
        short carriageNumber,
        int seatCount
) {
}
