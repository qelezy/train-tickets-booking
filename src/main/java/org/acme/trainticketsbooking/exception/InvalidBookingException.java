package org.acme.trainticketsbooking.exception;

import jakarta.ws.rs.core.Response;

public class InvalidBookingException extends BusinessException {

    public InvalidBookingException(String message) {
        super(Response.Status.BAD_REQUEST, "Некорректный запрос", message);
    }
}
