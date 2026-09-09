package org.acme.trainticketsbooking.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.trainticketsbooking.domain.SeatAvailability;
import org.acme.trainticketsbooking.domain.TripSchedule;
import org.acme.trainticketsbooking.dto.response.SeatAvailabilityResponse;
import org.acme.trainticketsbooking.dto.response.TripSearchResponse;

import java.util.List;

@ApplicationScoped
public class TripMapper {

    public List<TripSearchResponse> toSearchResponseList(List<TripSchedule> schedules) {
        return schedules.stream().map(this::toSearchResponse).toList();
    }

    public TripSearchResponse toSearchResponse(TripSchedule schedule) {
        return new TripSearchResponse(
            schedule.tripId(),
            schedule.trainNumber(),
            schedule.trainName(),
            schedule.departureStationName(),
            schedule.departureCity(),
            schedule.arrivalStationName(),
            schedule.arrivalCity(),
            schedule.departureTime(),
            schedule.arrivalTime(),
            toSeatAvailabilityResponseList(schedule.availableSeats())
        );
    }

    private List<SeatAvailabilityResponse> toSeatAvailabilityResponseList(List<SeatAvailability> seats) {
        return seats.stream().map(this::toSeatAvailabilityResponse).toList();
    }

    private SeatAvailabilityResponse toSeatAvailabilityResponse(SeatAvailability seat) {
        return new SeatAvailabilityResponse(seat.carriageType(), seat.availableSeatsCount());
    }
}
