package org.acme.trainticketsbooking.domain;

public record OccupiedSeat(
        long tripCarriageId,
        short seatNumber
) {
}
