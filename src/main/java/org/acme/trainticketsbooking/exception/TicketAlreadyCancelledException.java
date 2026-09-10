package org.acme.trainticketsbooking.exception;

import jakarta.ws.rs.core.Response;

import java.util.UUID;

public class TicketAlreadyCancelledException extends BusinessException {

    public TicketAlreadyCancelledException(UUID ticketId) {
        super(Response.Status.CONFLICT, "Конфликт", "Билет уже отменён: " + ticketId);
    }
}
