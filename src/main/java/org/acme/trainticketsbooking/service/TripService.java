package org.acme.trainticketsbooking.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.trainticketsbooking.domain.SeatAvailability;
import org.acme.trainticketsbooking.domain.TripSchedule;
import org.acme.trainticketsbooking.dto.request.TripSearchRequest;
import org.acme.trainticketsbooking.dto.response.TripSearchResponse;
import org.acme.trainticketsbooking.mapper.TripMapper;
import org.acme.trainticketsbooking.repository.TripRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class TripService {

    private final TripRepository tripRepository;
    private final TripMapper tripMapper;

    public TripService(TripRepository tripRepository, TripMapper tripMapper) {
        this.tripRepository = tripRepository;
        this.tripMapper = tripMapper;
    }

    public List<TripSearchResponse> findAvailableTrips(TripSearchRequest request) {
        List<TripSchedule> schedules = tripRepository.findSchedules(
            request.departureCity(),
            request.arrivalCity(),
            request.trainName()
        );

        if (schedules.isEmpty()) {
            return List.of();
        }

        List<Long> tripIds = schedules.stream().map(TripSchedule::id).toList();
        Map<Long, List<SeatAvailability>> availabilityByTripId =
            tripRepository.findSeatAvailabilityByTripIds(tripIds);

        List<TripSchedule> result = new ArrayList<>(schedules.size());
        for (TripSchedule schedule : schedules) {
            List<SeatAvailability> seats = availabilityByTripId.getOrDefault(schedule.id(), List.of());
            result.add(schedule.withAvailableSeats(seats));
        }
        return tripMapper.toSearchResponseList(result);
    }
}
