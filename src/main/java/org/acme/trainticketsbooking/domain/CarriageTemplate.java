package org.acme.trainticketsbooking.domain;

public record CarriageTemplate(
        Long id,
        CarriageType type,
        short seatCount
) {
}
