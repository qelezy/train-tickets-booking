package org.acme.trainticketsbooking.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.acme.trainticketsbooking.support.TestDataHelper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@QuarkusTest
class TicketResourceTest {

    private static final short CARRIAGE = 1;

    @Inject
    TestDataHelper testDataHelper;

    @Test
    void shouldCancelOneTicketFromGroupBooking() {
        long tripId = testDataHelper.createCancellableTrip();
        var seats = testDataHelper.findFreeSeats(tripId, CARRIAGE, 2);
        var created = createGroupBooking(tripId, seats.get(0), seats.get(1));
        String ticketId = created.ticketIds().get(0);

        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post("/api/v1/tickets/{ticketId}/cancel", ticketId)
        .then()
            .statusCode(200)
            .body("ticketId", equalTo(ticketId))
            .body("status", equalTo("CANCELLED"))
            .body("bookingId", equalTo(created.bookingId()))
            .body("bookingStatus", equalTo("CONFIRMED"));
    }

    @Test
    void shouldFreeSeatAfterTicketCancel() {
        long tripId = testDataHelper.createCancellableTrip();
        short seatNumber = testDataHelper.findFreeSeat(tripId, CARRIAGE);
        var created = createGroupBooking(tripId, seatNumber);
        String ticketId = created.ticketIds().get(0);

        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post("/api/v1/tickets/{ticketId}/cancel", ticketId)
        .then()
            .statusCode(200);

        given()
            .contentType(ContentType.JSON)
            .body(bookingBody(tripId, List.of(seat(CARRIAGE, seatNumber))))
        .when()
            .post("/api/v1/bookings")
        .then()
            .statusCode(201)
            .body("tickets", hasSize(1));
    }

    @Test
    void shouldCancelBookingWhenLastTicketCancelled() {
        long tripId = testDataHelper.createCancellableTrip();
        var seats = testDataHelper.findFreeSeats(tripId, CARRIAGE, 2);
        var created = createGroupBooking(tripId, seats.get(0), seats.get(1));

        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post("/api/v1/tickets/{ticketId}/cancel", created.ticketIds().get(0))
        .then()
            .statusCode(200)
            .body("bookingStatus", equalTo("CONFIRMED"));

        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post("/api/v1/tickets/{ticketId}/cancel", created.ticketIds().get(1))
        .then()
            .statusCode(200)
            .body("status", equalTo("CANCELLED"))
            .body("bookingStatus", equalTo("CANCELLED"));
    }

    @Test
    void shouldRejectAlreadyCancelledTicket() {
        long tripId = testDataHelper.createCancellableTrip();
        short seatNumber = testDataHelper.findFreeSeat(tripId, CARRIAGE);
        var created = createGroupBooking(tripId, seatNumber);
        String ticketId = created.ticketIds().get(0);

        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post("/api/v1/tickets/{ticketId}/cancel", ticketId)
        .then()
            .statusCode(200);

        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post("/api/v1/tickets/{ticketId}/cancel", ticketId)
        .then()
            .statusCode(409)
            .body("error", equalTo("Конфликт"));
    }

    @Test
    void shouldRejectCancelWhenLessThanTwoHoursBeforeDeparture() {
        long tripId = testDataHelper.createTripTooLateForCancellation();
        short seatNumber = testDataHelper.findFreeSeat(tripId, CARRIAGE);
        var created = createGroupBooking(tripId, seatNumber);

        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post("/api/v1/tickets/{ticketId}/cancel", created.ticketIds().get(0))
        .then()
            .statusCode(400)
            .body("message", equalTo("Отмена возможна не позднее чем за 2 часа до отправления"));
    }

    @Test
    void shouldReturnNotFoundForUnknownTicket() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post("/api/v1/tickets/{ticketId}/cancel", UUID.randomUUID())
        .then()
            .statusCode(404)
            .body("error", equalTo("Не найдено"));
    }

    @Test
    void shouldRejectInvalidTicketId() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post("/api/v1/tickets/{ticketId}/cancel", "not-a-uuid")
        .then()
            .statusCode(400)
            .body("message", equalTo("Некорректный идентификатор билета"));
    }

    private CreatedBookingIds createGroupBooking(long tripId, short... seatNumbers) {
        List<Map<String, Object>> seats = new ArrayList<>(seatNumbers.length);
        for (short seatNumber : seatNumbers) {
            seats.add(seat(CARRIAGE, seatNumber));
        }

        var response = given()
            .contentType(ContentType.JSON)
            .body(bookingBody(tripId, seats))
        .when()
            .post("/api/v1/bookings")
        .then()
            .statusCode(201)
            .extract();

        String bookingId = response.path("bookingId");
        List<String> ticketIds = response.path("tickets.ticketId");
        return new CreatedBookingIds(bookingId, ticketIds);
    }

    private static Map<String, Object> bookingBody(long tripId, List<Map<String, Object>> seats) {
        return Map.of(
            "tripId", tripId,
            "seats", seats
        );
    }

    private static Map<String, Object> seat(short carriageNumber, short seatNumber) {
        return Map.of(
            "carriageNumber", carriageNumber,
            "seatNumber", seatNumber
        );
    }

    private record CreatedBookingIds(String bookingId, List<String> ticketIds) {
    }
}
