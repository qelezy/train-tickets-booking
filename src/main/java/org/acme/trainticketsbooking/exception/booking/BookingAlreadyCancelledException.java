package org.acme.trainticketsbooking.exception.booking;

import jakarta.ws.rs.core.Response;
import org.acme.trainticketsbooking.exception.common.BusinessException;

import java.util.UUID;

public class BookingAlreadyCancelledException extends BusinessException {

    public BookingAlreadyCancelledException(UUID bookingId) {
        super(Response.Status.CONFLICT, "Конфликт", "Бронирование уже отменено: " + bookingId);
    }
}
