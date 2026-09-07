package org.acme.trainticketsbooking.domain;

import java.util.UUID;

public record CreatedTicket(
        UUID ticketId,
        short carriageNumber,
        short seatNumber,
        TicketStatus status
) {
}
