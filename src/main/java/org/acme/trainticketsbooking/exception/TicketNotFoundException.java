package org.acme.trainticketsbooking.exception;

import jakarta.ws.rs.core.Response;

import java.util.UUID;

public class TicketNotFoundException extends BusinessException {

    public TicketNotFoundException(UUID ticketId) {
        super(Response.Status.NOT_FOUND, "Не найдено", "Билет не найден: " + ticketId);
    }
}
