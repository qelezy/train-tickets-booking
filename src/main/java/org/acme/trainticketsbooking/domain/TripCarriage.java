package org.acme.trainticketsbooking.domain;

public record TripCarriage(
        Long id,
        Long tripId,
        Long carriageTemplateId,
        short number
) {
}
