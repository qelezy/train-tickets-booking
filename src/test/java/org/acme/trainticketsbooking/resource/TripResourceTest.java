package org.acme.trainticketsbooking.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

@QuarkusTest
class TripResourceTest {

    @Test
    void shouldReturnPobedaTripsFromLipetskToMoscow() {
        given()
            .queryParam("from", "Липецк")
            .queryParam("to", "Москва")
            .queryParam("trainName", "Победа")
        .when()
            .get("/api/v1/trips")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("size()", greaterThan(0))
            .body("[0].trainName", equalTo("Победа"))
            .body("[0].departureCity", equalTo("Липецк"))
            .body("[0].arrivalCity", equalTo("Москва"))
            .body("[0].availableSeats.size()", greaterThan(0));
    }

    @Test
    void shouldReturnEmptyListForUnknownRoute() {
        given()
            .queryParam("from", "Калуга")
            .queryParam("to", "Тула")
        .when()
            .get("/api/v1/trips")
        .then()
            .statusCode(200)
            .body("$", empty());
    }

    @Test
    void shouldRejectBlankDepartureCity() {
        given()
            .queryParam("from", " ")
            .queryParam("to", "Москва")
        .when()
            .get("/api/v1/trips")
        .then()
            .statusCode(400)
            .body("status", equalTo(400))
            .body("error", equalTo("Некорректный запрос"))
            .body("message", equalTo("Город отправления обязателен"));
    }

    @Test
    void shouldRejectSameDepartureAndArrivalCities() {
        given()
            .queryParam("from", "Москва")
            .queryParam("to", "москва")
        .when()
            .get("/api/v1/trips")
        .then()
            .statusCode(400)
            .body("message", equalTo("Город отправления и город прибытия не могут совпадать"));
    }

    @Test
    void shouldRejectMissingArrivalCity() {
        given()
            .queryParam("from", "Липецк")
        .when()
            .get("/api/v1/trips")
        .then()
            .statusCode(400)
            .body("message", equalTo("Город прибытия обязателен"));
    }
}
