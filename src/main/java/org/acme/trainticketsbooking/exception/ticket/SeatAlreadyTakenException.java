package org.acme.trainticketsbooking.exception.ticket;

import jakarta.ws.rs.core.Response;
import org.acme.trainticketsbooking.exception.common.BusinessException;

public class SeatAlreadyTakenException extends BusinessException {

    public SeatAlreadyTakenException(short carriageNumber, short seatNumber) {
        super(
            Response.Status.CONFLICT,
            "Конфликт",
            "Место уже занято: вагон " + carriageNumber + ", место " + seatNumber
        );
    }
}
