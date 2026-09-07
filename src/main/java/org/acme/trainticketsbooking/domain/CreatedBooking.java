package org.acme.trainticketsbooking.domain;

import java.util.List;
import java.util.UUID;

public record CreatedBooking(
        UUID bookingId,
        List<CreatedTicket> tickets
) {
}
