package org.acme.trainticketsbooking.resource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.trainticketsbooking.dto.request.TripSearchRequest;
import org.acme.trainticketsbooking.service.TripService;

@Path("/api/v1/trips")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TripResource {

    private final TripService tripService;

    public TripResource(TripService tripService) {
        this.tripService = tripService;
    }

    @GET
    public Response getAvailableTrips(@BeanParam TripSearchRequest request) {
        validate(request);
        return Response.ok(tripService.findAvailableTrips(request)).build();
    }

    private void validate(TripSearchRequest request) {
        if (request.departureCity() == null || request.departureCity().isBlank()) {
            throw new BadRequestException("Город отправления обязателен");
        }
        if (request.arrivalCity() == null || request.arrivalCity().isBlank()) {
            throw new BadRequestException("Город прибытия обязателен");
        }
        if (request.departureCity().equalsIgnoreCase(request.arrivalCity())) {
            throw new BadRequestException("Город отправления и город прибытия не могут совпадать");
        }
    }
}
