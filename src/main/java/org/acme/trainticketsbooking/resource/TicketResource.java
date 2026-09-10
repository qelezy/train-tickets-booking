package org.acme.trainticketsbooking.resource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.trainticketsbooking.service.BookingService;

import java.util.UUID;

@Path("/api/v1/tickets")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TicketResource {

    private final BookingService bookingService;

    public TicketResource(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @POST
    @Path("{ticketId}/cancel")
    public Response cancelTicket(@PathParam("ticketId") String ticketId) {
        return Response.ok(bookingService.cancelTicket(parseTicketId(ticketId))).build();
    }

    private UUID parseTicketId(String ticketId) {
        if (ticketId == null || ticketId.isBlank()) {
            throw new BadRequestException("Идентификатор билета обязателен");
        }
        try {
            return UUID.fromString(ticketId.trim());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Некорректный идентификатор билета");
        }
    }
}
