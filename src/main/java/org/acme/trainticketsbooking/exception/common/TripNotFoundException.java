package org.acme.trainticketsbooking.exception.common;

import jakarta.ws.rs.core.Response;

public class TripNotFoundException extends BusinessException {

    public TripNotFoundException(long tripId) {
        super(Response.Status.NOT_FOUND, "Не найдено", "Рейс не найден: " + tripId);
    }
}
