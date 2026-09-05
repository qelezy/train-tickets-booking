package org.acme.trainticketsbooking.dto.request;

import org.jboss.resteasy.reactive.RestQuery;

public record TripSearchRequest(
        @RestQuery("from") String departureCity,
        @RestQuery("to") String arrivalCity,
        @RestQuery("trainName") String trainName
) {
    public TripSearchRequest {
        departureCity = departureCity != null ? departureCity.trim() : null;
        arrivalCity = arrivalCity != null ? arrivalCity.trim() : null;
        trainName = (trainName != null && !trainName.isBlank()) ? trainName.trim() : null;
    }
}
