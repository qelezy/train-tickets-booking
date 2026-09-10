package org.acme.trainticketsbooking.exception.booking;

import jakarta.ws.rs.core.Response;
import org.acme.trainticketsbooking.exception.common.BusinessException;

public class InvalidBookingException extends BusinessException {

    public InvalidBookingException(String message) {
        super(Response.Status.BAD_REQUEST, "Некорректный запрос", message);
    }
}
