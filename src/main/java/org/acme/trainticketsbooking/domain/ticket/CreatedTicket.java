package org.acme.trainticketsbooking.domain.ticket;

import java.util.UUID;

public record CreatedTicket(
        UUID ticketId,
        short carriageNumber,
        short seatNumber,
        TicketStatus status
) {
}
