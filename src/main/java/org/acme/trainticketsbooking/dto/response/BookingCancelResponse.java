package org.acme.trainticketsbooking.dto.response;

import org.acme.trainticketsbooking.domain.booking.BookingStatus;

import java.util.UUID;

public record BookingCancelResponse(
        UUID bookingId,
        BookingStatus status
) {
}
