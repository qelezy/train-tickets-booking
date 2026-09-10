package org.acme.trainticketsbooking.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.acme.trainticketsbooking.support.TestDataHelper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class BookingResourceTest {

    private static final short CARRIAGE = 1;

    @Inject
    TestDataHelper testDataHelper;

    @Test
    void shouldCreateGroupBooking() {
        long tripId = testDataHelper.findPobedaTripId();
        var seats = testDataHelper.findFreeSeats(tripId, CARRIAGE, 2);

        given()
            .contentType(ContentType.JSON)
            .body(bookingBody(tripId, List.of(
                seat(CARRIAGE, seats.get(0)),
                seat(CARRIAGE, seats.get(1))
            )))
        .when()
            .post("/api/v1/bookings")
        .then()
            .statusCode(201)
            .body("bookingId", notNullValue())
            .body("tickets", hasSize(2))
            .body("tickets[0].status", equalTo("ACTIVE"));
    }

    @Test
    void shouldRejectAlreadyTakenSeat() {
        long tripId = testDataHelper.findPobedaTripId();
        short seatNumber = testDataHelper.findFreeSeat(tripId, CARRIAGE);
        Map<String, Object> body = bookingBody(tripId, List.of(seat(CARRIAGE, seatNumber)));

        given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/api/v1/bookings")
        .then()
            .statusCode(201);

        given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/api/v1/bookings")
        .then()
            .statusCode(409)
            .body("error", equalTo("Конфликт"))
            .body("message", equalTo("Место уже занято: вагон 1, место " + seatNumber));
    }

    @Test
    void shouldCancelBooking() {
        long tripId = testDataHelper.createCancellableTrip();
        short seatNumber = testDataHelper.findFreeSeat(tripId, CARRIAGE);
        String bookingId = createBooking(tripId, seatNumber);

        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post("/api/v1/bookings/{bookingId}/cancel", bookingId)
        .then()
            .statusCode(200)
            .body("bookingId", equalTo(bookingId))
            .body("status", equalTo("CANCELLED"));
    }

    @Test
    void shouldRejectCancelForAlreadyCancelledBooking() {
        long tripId = testDataHelper.createCancellableTrip();
        short seatNumber = testDataHelper.findFreeSeat(tripId, CARRIAGE);
        String bookingId = createBooking(tripId, seatNumber);

        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post("/api/v1/bookings/{bookingId}/cancel", bookingId)
        .then()
            .statusCode(200);

        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post("/api/v1/bookings/{bookingId}/cancel", bookingId)
        .then()
            .statusCode(409)
            .body("error", equalTo("Конфликт"));
    }

    @Test
    void shouldRejectCancelWhenLessThanTwoHoursBeforeDeparture() {
        long tripId = testDataHelper.createTripTooLateForCancellation();
        short seatNumber = testDataHelper.findFreeSeat(tripId, CARRIAGE);
        String bookingId = createBooking(tripId, seatNumber);

        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post("/api/v1/bookings/{bookingId}/cancel", bookingId)
        .then()
            .statusCode(400)
            .body("message", equalTo("Отмена возможна не позднее чем за 2 часа до отправления"));
    }

    @Test
    void shouldReturnNotFoundForUnknownBooking() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post("/api/v1/bookings/{bookingId}/cancel", UUID.randomUUID())
        .then()
            .statusCode(404)
            .body("error", equalTo("Не найдено"));
    }

    @Test
    void shouldRejectInvalidBookingPayload() {
        given()
            .contentType(ContentType.JSON)
            .body(Map.of("tripId", 1, "seats", List.of()))
        .when()
            .post("/api/v1/bookings")
        .then()
            .statusCode(400)
            .body("message", equalTo("Список мест не может быть пустым"));
    }

    @Test
    void shouldRejectUnknownTrip() {
        given()
            .contentType(ContentType.JSON)
            .body(bookingBody(999_999L, List.of(seat(CARRIAGE, (short) 1))))
        .when()
            .post("/api/v1/bookings")
        .then()
            .statusCode(404)
            .body("error", equalTo("Не найдено"));
    }

    @Test
    void shouldRejectInvalidBookingId() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post("/api/v1/bookings/{bookingId}/cancel", "not-a-uuid")
        .then()
            .statusCode(400)
            .body("message", equalTo("Некорректный идентификатор бронирования"));
    }

    private String createBooking(long tripId, short seatNumber) {
        return given()
            .contentType(ContentType.JSON)
            .body(bookingBody(tripId, List.of(seat(CARRIAGE, seatNumber))))
        .when()
            .post("/api/v1/bookings")
        .then()
            .statusCode(201)
            .extract()
            .path("bookingId");
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
}
