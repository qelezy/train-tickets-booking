package org.acme.trainticketsbooking.dto.request;

public record SeatRequest(
        Short carriageNumber,
        Short seatNumber
) {
}
