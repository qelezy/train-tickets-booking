package org.acme.trainticketsbooking.repository;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.trainticketsbooking.domain.CarriageType;
import org.acme.trainticketsbooking.domain.SeatAvailability;
import org.acme.trainticketsbooking.domain.TripSchedule;
import org.acme.trainticketsbooking.exception.DataAccessException;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class TripRepository {

    private final AgroalDataSource dataSource;

    private static final String FIND_TRIPS_SQL = """
        SELECT tr.id AS trip_id,
               t.number AS train_number,
               t.name AS train_name,
               ds.name AS departure_station_name,
               ds.city AS departure_city,
               arrs.name AS arrival_station_name,
               arrs.city AS arrival_city,
               tr.departure_time,
               tr.arrival_time
        FROM trip tr
        JOIN train t ON t.id = tr.train_id
        JOIN station ds ON ds.id = t.departure_station_id
        JOIN station arrs ON arrs.id = t.arrival_station_id
        WHERE LOWER(ds.city) = LOWER(?)
          AND LOWER(arrs.city) = LOWER(?)
          AND (? IS NULL OR LOWER(t.name) = LOWER(?))
        ORDER BY tr.departure_time ASC
        """;

    private static final String FIND_SEAT_AVAILABILITY_SQL = """
        SELECT tc.trip_id,
               ct.type AS carriage_type,
               COALESCE(SUM(ct.seat_count), 0) - COALESCE(SUM(occ.cnt), 0) AS available_seats
        FROM trip_carriage tc
        JOIN carriage_template ct ON ct.id = tc.carriage_template_id
        LEFT JOIN (
            SELECT trip_carriage_id, COUNT(*)::int AS cnt
            FROM ticket
            WHERE status = 'ACTIVE'
            GROUP BY trip_carriage_id
        ) occ ON occ.trip_carriage_id = tc.id
        WHERE tc.trip_id = ANY(?)
        GROUP BY tc.trip_id, ct.type
        ORDER BY tc.trip_id ASC, ct.type ASC
        """;

    public TripRepository(AgroalDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<TripSchedule> findSchedules(String departureCity, String arrivalCity, String trainName) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_TRIPS_SQL)) {

            statement.setString(1, departureCity);
            statement.setString(2, arrivalCity);
            statement.setString(3, trainName);
            statement.setString(4, trainName);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<TripSchedule> schedules = new ArrayList<>();
                while (resultSet.next()) {
                    schedules.add(mapTripSchedule(resultSet));
                }
                return schedules;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка при поиске рейсов", e);
        }
    }

    public Map<Long, List<SeatAvailability>> findSeatAvailabilityByTripIds(Collection<Long> tripIds) {
        if (tripIds == null || tripIds.isEmpty()) {
            return Map.of();
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_SEAT_AVAILABILITY_SQL)) {

            Array tripIdArray = connection.createArrayOf("bigint", tripIds.toArray(Long[]::new));
            statement.setArray(1, tripIdArray);

            try (ResultSet resultSet = statement.executeQuery()) {
                Map<Long, List<SeatAvailability>> availabilityByTripId = new LinkedHashMap<>();
                while (resultSet.next()) {
                    long tripId = resultSet.getLong("trip_id");
                    SeatAvailability availability = new SeatAvailability(
                        CarriageType.valueOf(resultSet.getString("carriage_type")),
                        resultSet.getInt("available_seats")
                    );
                    availabilityByTripId
                        .computeIfAbsent(tripId, id -> new ArrayList<>())
                        .add(availability);
                }
                return availabilityByTripId;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка при получении свободных мест", e);
        }
    }

    private TripSchedule mapTripSchedule(ResultSet resultSet) throws SQLException {
        return new TripSchedule(
            resultSet.getLong("trip_id"),
            resultSet.getString("train_number"),
            resultSet.getString("train_name"),
            resultSet.getString("departure_station_name"),
            resultSet.getString("departure_city"),
            resultSet.getString("arrival_station_name"),
            resultSet.getString("arrival_city"),
            resultSet.getObject("departure_time", OffsetDateTime.class),
            resultSet.getObject("arrival_time", OffsetDateTime.class),
            List.of()
        );
    }
}
