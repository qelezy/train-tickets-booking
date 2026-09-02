package org.acme.trainticketsbooking.domain;

public record Station(
        Long id,
        String code,
        String name,
        String city
) {
}
