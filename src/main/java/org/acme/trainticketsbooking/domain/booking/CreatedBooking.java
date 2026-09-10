package org.acme.trainticketsbooking.domain.booking;

import org.acme.trainticketsbooking.domain.ticket.CreatedTicket;

import java.util.List;
import java.util.UUID;

public record CreatedBooking(
        UUID bookingId,
        List<CreatedTicket> tickets
) {
}
