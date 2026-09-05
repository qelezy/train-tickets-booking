package org.acme.trainticketsbooking.dto.response;

import java.time.OffsetDateTime;
import java.util.List;

public record TripSearchResponse(
        Long tripId,
        String trainNumber,
        String trainName,
        String departureStationName,
        String departureCity,
        String arrivalStationName,
        String arrivalCity,
        OffsetDateTime departureTime,
        OffsetDateTime arrivalTime,
        List<SeatAvailabilityResponse> availableSeats
) {
}
