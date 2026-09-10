package org.acme.trainticketsbooking.domain.ticket;

public record SeatSelection(
        long tripCarriageId,
        short carriageNumber,
        short seatNumber
) {
}
