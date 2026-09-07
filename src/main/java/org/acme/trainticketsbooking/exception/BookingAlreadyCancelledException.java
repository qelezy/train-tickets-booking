package org.acme.trainticketsbooking.exception;

import jakarta.ws.rs.core.Response;

import java.util.UUID;

public class BookingAlreadyCancelledException extends BusinessException {

    public BookingAlreadyCancelledException(UUID bookingId) {
        super(Response.Status.CONFLICT, "Конфликт", "Бронирование уже отменено: " + bookingId);
    }
}
