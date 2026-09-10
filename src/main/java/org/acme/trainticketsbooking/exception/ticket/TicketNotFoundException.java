package org.acme.trainticketsbooking.exception.ticket;

import jakarta.ws.rs.core.Response;
import org.acme.trainticketsbooking.exception.common.BusinessException;

import java.util.UUID;

public class TicketNotFoundException extends BusinessException {

    public TicketNotFoundException(UUID ticketId) {
        super(Response.Status.NOT_FOUND, "Не найдено", "Билет не найден: " + ticketId);
    }
}
