package org.acme.trainticketsbooking.dto.request;

import java.util.List;

public record BookingCreateRequest(
        Long tripId,
        List<SeatRequest> seats
) {
}
