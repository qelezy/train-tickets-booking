package org.acme.trainticketsbooking.domain;

public record SeatSelection(
        long tripCarriageId,
        short carriageNumber,
        short seatNumber
) {
}
