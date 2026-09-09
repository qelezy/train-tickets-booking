package org.acme.trainticketsbooking.domain;

import java.time.OffsetDateTime;
import java.util.List;

public record TripSchedule(
        Long tripId,
        String trainNumber,
        String trainName,
        String departureStationName,
        String departureCity,
        String arrivalStationName,
        String arrivalCity,
        OffsetDateTime departureTime,
        OffsetDateTime arrivalTime,
        List<SeatAvailability> availableSeats
) {
    public TripSchedule {
        availableSeats = availableSeats == null ? List.of() : List.copyOf(availableSeats);
    }

    public TripSchedule withAvailableSeats(List<SeatAvailability> seats) {
        return new TripSchedule(
            tripId,
            trainNumber,
            trainName,
            departureStationName,
            departureCity,
            arrivalStationName,
            arrivalCity,
            departureTime,
            arrivalTime,
            seats
        );
    }
}
