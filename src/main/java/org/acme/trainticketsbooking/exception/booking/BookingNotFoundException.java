package org.acme.trainticketsbooking.exception.booking;

import jakarta.ws.rs.core.Response;
import org.acme.trainticketsbooking.exception.common.BusinessException;

import java.util.UUID;

public class BookingNotFoundException extends BusinessException {

    public BookingNotFoundException(UUID bookingId) {
        super(Response.Status.NOT_FOUND, "Не найдено", "Бронирование не найдено: " + bookingId);
    }
}
