package org.acme.trainticketsbooking.dto.response;

import java.util.List;
import java.util.UUID;

public record BookingCreateResponse(
        UUID bookingId,
        List<TicketResponse> tickets
) {
}
