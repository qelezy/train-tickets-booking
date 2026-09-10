package org.acme.trainticketsbooking.exception.ticket;

import jakarta.ws.rs.core.Response;
import org.acme.trainticketsbooking.exception.common.BusinessException;

import java.util.UUID;

public class TicketAlreadyCancelledException extends BusinessException {

    public TicketAlreadyCancelledException(UUID ticketId) {
        super(Response.Status.CONFLICT, "Конфликт", "Билет уже отменён: " + ticketId);
    }
}
