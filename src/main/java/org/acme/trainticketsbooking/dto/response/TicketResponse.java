package org.acme.trainticketsbooking.dto.response;

import org.acme.trainticketsbooking.domain.ticket.TicketStatus;

import java.util.UUID;

public record TicketResponse(
        UUID ticketId,
        short carriageNumber,
        short seatNumber,
        TicketStatus status
) {
}
