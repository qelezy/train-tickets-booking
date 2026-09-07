package org.acme.trainticketsbooking.domain;

import java.util.UUID;

public record CancelledBooking(
        UUID bookingId,
        BookingStatus status
) {
}
