package org.acme.trainticketsbooking.domain.booking;

import java.util.UUID;

public record CancelledBooking(
        UUID bookingId,
        BookingStatus status
) {
}
