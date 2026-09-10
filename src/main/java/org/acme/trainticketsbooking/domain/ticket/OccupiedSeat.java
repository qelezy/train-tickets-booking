package org.acme.trainticketsbooking.domain.ticket;

public record OccupiedSeat(
        long tripCarriageId,
        short seatNumber
) {
}
