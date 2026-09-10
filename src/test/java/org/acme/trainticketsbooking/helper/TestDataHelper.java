package org.acme.trainticketsbooking.support;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class TestDataHelper {

    private final AgroalDataSource dataSource;

    public TestDataHelper(AgroalDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public long findPobedaTripId() {
        String sql = """
            SELECT tr.id
            FROM trip tr
            JOIN train t ON t.id = tr.train_id
            JOIN station dep ON dep.id = t.departure_station_id
            JOIN station arr ON arr.id = t.arrival_station_id
            WHERE dep.city = 'Липецк'
              AND arr.city = 'Москва'
              AND t.name = 'Победа'
              AND tr.departure_time > NOW()
            ORDER BY tr.departure_time ASC
            LIMIT 1
            """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (!resultSet.next()) {
                throw new IllegalStateException("Не найден рейс Победа Липецк-Москва");
            }
            return resultSet.getLong(1);
        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось получить рейс для теста", e);
        }
    }

    public long createCancellableTrip() {
        return createTripDepartingIn(Duration.ofHours(5));
    }

    public long createTripTooLateForCancellation() {
        return createTripDepartingIn(Duration.ofMinutes(30));
    }

    public short findFreeSeat(long tripId, short carriageNumber) {
        List<Short> seats = findFreeSeats(tripId, carriageNumber, 1);
        return seats.getFirst();
    }

    public List<Short> findFreeSeats(long tripId, short carriageNumber, int limit) {
        String sql = """
            SELECT seat.seat_number
            FROM generate_series(1, 54) AS seat(seat_number)
            WHERE NOT EXISTS (
                SELECT 1
                FROM ticket t
                JOIN trip_carriage tc ON tc.id = t.trip_carriage_id
                WHERE tc.trip_id = ?
                  AND tc.number = ?
                  AND t.seat_number = seat.seat_number
                  AND t.status = 'ACTIVE'
            )
            ORDER BY seat.seat_number
            LIMIT ?
            """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, tripId);
            statement.setShort(2, carriageNumber);
            statement.setInt(3, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Short> seats = new ArrayList<>();
                while (resultSet.next()) {
                    seats.add(resultSet.getShort(1));
                }
                if (seats.size() < limit) {
                    throw new IllegalStateException("Недостаточно свободных мест для теста");
                }
                return seats;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось найти свободные места", e);
        }
    }

    private long createTripDepartingIn(Duration untilDeparture) {
        String insertTrip = """
            INSERT INTO trip (train_id, departure_time, arrival_time)
            SELECT t.id, ?, ?
            FROM train t
            WHERE t.number = '737А'
            RETURNING id
            """;
        OffsetDateTime departure = OffsetDateTime.now().plus(untilDeparture);
        OffsetDateTime arrival = departure.plusHours(6);

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long tripId;
                try (PreparedStatement statement = connection.prepareStatement(insertTrip)) {
                    statement.setObject(1, departure);
                    statement.setObject(2, arrival);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        resultSet.next();
                        tripId = resultSet.getLong(1);
                    }
                }

                try (Statement statement = connection.createStatement()) {
                    statement.execute("""
                        INSERT INTO trip_carriage (trip_id, carriage_template_id, number)
                        SELECT %d, ct.id, 1
                        FROM carriage_template ct
                        WHERE ct.type = 'PLATSKART'
                        ORDER BY ct.id
                        LIMIT 1
                        """.formatted(tripId));
                }

                connection.commit();
                return tripId;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось создать рейс для теста", e);
        }
    }
}
