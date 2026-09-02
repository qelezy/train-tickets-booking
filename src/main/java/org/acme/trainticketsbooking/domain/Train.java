package org.acme.trainticketsbooking.domain;

public record Train(
        Long id,
        String number,
        String name,
        Long departureStationId,
        Long arrivalStationId
) {
}
