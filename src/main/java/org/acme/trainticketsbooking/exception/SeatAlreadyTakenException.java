package org.acme.trainticketsbooking.exception;

import jakarta.ws.rs.core.Response;

public class SeatAlreadyTakenException extends BusinessException {

    public SeatAlreadyTakenException(short carriageNumber, short seatNumber) {
        super(
            Response.Status.CONFLICT,
            "Конфликт",
            "Место уже занято: вагон " + carriageNumber + ", место " + seatNumber
        );
    }
}
