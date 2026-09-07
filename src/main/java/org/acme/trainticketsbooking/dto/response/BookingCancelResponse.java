package org.acme.trainticketsbooking.dto.response;

import org.acme.trainticketsbooking.domain.BookingStatus;

import java.util.UUID;

public record BookingCancelResponse(
        UUID bookingId,
        BookingStatus status
) {
}
