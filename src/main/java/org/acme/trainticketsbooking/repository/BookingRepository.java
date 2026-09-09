package org.acme.trainticketsbooking.repository;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.trainticketsbooking.domain.BookingForCancellation;
import org.acme.trainticketsbooking.domain.BookingStatus;
import org.acme.trainticketsbooking.domain.CreatedTicket;
import org.acme.trainticketsbooking.domain.OccupiedSeat;
import org.acme.trainticketsbooking.domain.SeatSelection;
import org.acme.trainticketsbooking.domain.TicketStatus;
import org.acme.trainticketsbooking.domain.TripCarriageSeatInfo;
import org.acme.trainticketsbooking.exception.DataAccessException;
import org.acme.trainticketsbooking.exception.SeatAlreadyTakenException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class BookingRepository {

    private static final String UNIQUE_VIOLATION = "23505";

    private static final String LOCK_TRIP_WITH_CARRIAGES_SQL = """
        SELECT t.id AS trip_id,
               tc.id AS trip_carriage_id,
               tc.number AS carriage_number,
               ct.seat_count
        FROM trip t
        LEFT JOIN trip_carriage tc ON tc.trip_id = t.id
        LEFT JOIN carriage_template ct ON ct.id = tc.carriage_template_id
        WHERE t.id = ?
          AND t.departure_time > NOW()
        FOR SHARE OF t
        """;

    private static final String FIND_ACTIVE_SEATS_SQL = """
        SELECT trip_carriage_id, seat_number
        FROM ticket
        WHERE status = 'ACTIVE'
          AND trip_carriage_id = ANY(?)
        """;

    private static final String INSERT_BOOKING_SQL = """
        INSERT INTO booking (status)
        VALUES ('CONFIRMED')
        RETURNING id
        """;

    private static final String INSERT_TICKET_SQL = """
        INSERT INTO ticket (booking_id, trip_carriage_id, seat_number, status)
        VALUES (?, ?, ?, 'ACTIVE')
        RETURNING id
        """;

    private static final String LOCK_BOOKING_FOR_CANCELLATION_SQL = """
        SELECT b.id AS booking_id,
               b.status,
               NOW() AS checked_at,
               (
                   SELECT MIN(tr.departure_time)
                   FROM ticket t
                   JOIN trip_carriage tc ON tc.id = t.trip_carriage_id
                   JOIN trip tr ON tr.id = tc.trip_id
                   WHERE t.booking_id = b.id
               ) AS departure_time
        FROM booking b
        WHERE b.id = ?
        FOR UPDATE
        """;

    private static final String CANCEL_BOOKING_SQL = """
        UPDATE booking
        SET status = 'CANCELLED',
            cancelled_at = NOW()
        WHERE id = ?
          AND status = 'CONFIRMED'
        """;

    private static final String CANCEL_TICKETS_SQL = """
        UPDATE ticket
        SET status = 'CANCELLED',
            cancelled_at = NOW()
        WHERE booking_id = ?
          AND status = 'ACTIVE'
        """;

    private final AgroalDataSource dataSource;

    public BookingRepository(AgroalDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Optional<List<TripCarriageSeatInfo>> lockTripWithCarriages(long tripId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(LOCK_TRIP_WITH_CARRIAGES_SQL)) {

            statement.setLong(1, tripId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                List<TripCarriageSeatInfo> carriages = new ArrayList<>();
                do {
                    long tripCarriageId = resultSet.getLong("trip_carriage_id");
                    if (!resultSet.wasNull()) {
                        carriages.add(new TripCarriageSeatInfo(
                            tripCarriageId,
                            resultSet.getShort("carriage_number"),
                            resultSet.getInt("seat_count")
                        ));
                    }
                } while (resultSet.next());

                return Optional.of(carriages);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка при получении вагонов рейса", e);
        }
    }

    public Set<OccupiedSeat> findActiveSeats(Collection<Long> tripCarriageIds) {
        if (tripCarriageIds == null || tripCarriageIds.isEmpty()) {
            return Set.of();
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ACTIVE_SEATS_SQL)) {

            statement.setArray(1, connection.createArrayOf("bigint", tripCarriageIds.toArray(Long[]::new)));
            try (ResultSet resultSet = statement.executeQuery()) {
                Set<OccupiedSeat> occupied = new HashSet<>();
                while (resultSet.next()) {
                    occupied.add(new OccupiedSeat(
                        resultSet.getLong("trip_carriage_id"),
                        resultSet.getShort("seat_number")
                    ));
                }
                return occupied;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка при проверке занятости мест", e);
        }
    }

    public UUID insertBooking() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_BOOKING_SQL);
             ResultSet resultSet = statement.executeQuery()) {

            resultSet.next();
            return resultSet.getObject("id", UUID.class);
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка при создании бронирования", e);
        }
    }

    public CreatedTicket insertTicket(UUID bookingId, SeatSelection seat) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_TICKET_SQL)) {

            statement.setObject(1, bookingId);
            statement.setLong(2, seat.tripCarriageId());
            statement.setShort(3, seat.seatNumber());
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return new CreatedTicket(
                    resultSet.getObject("id", UUID.class),
                    seat.carriageNumber(),
                    seat.seatNumber(),
                    TicketStatus.ACTIVE
                );
            }
        } catch (SQLException e) {
            if (isActiveSeatUniqueViolation(e)) {
                throw new SeatAlreadyTakenException(seat.carriageNumber(), seat.seatNumber());
            }
            throw new DataAccessException("Ошибка при создании билета", e);
        }
    }

    public Optional<BookingForCancellation> lockBookingForCancellation(UUID bookingId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(LOCK_BOOKING_FOR_CANCELLATION_SQL)) {

            statement.setObject(1, bookingId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(new BookingForCancellation(
                    resultSet.getObject("booking_id", UUID.class),
                    BookingStatus.valueOf(resultSet.getString("status")),
                    resultSet.getObject("departure_time", OffsetDateTime.class),
                    resultSet.getObject("checked_at", OffsetDateTime.class)
                ));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка при получении бронирования для отмены", e);
        }
    }

    public int cancelConfirmedBooking(UUID bookingId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(CANCEL_BOOKING_SQL)) {

            statement.setObject(1, bookingId);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка при отмене бронирования", e);
        }
    }

    public void cancelActiveTickets(UUID bookingId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(CANCEL_TICKETS_SQL)) {

            statement.setObject(1, bookingId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка при отмене билетов", e);
        }
    }

    private boolean isActiveSeatUniqueViolation(SQLException exception) {
        SQLException current = exception;
        while (current != null) {
            if (UNIQUE_VIOLATION.equals(current.getSQLState())) {
                return true;
            }
            current = current.getNextException();
        }
        return false;
    }
}
