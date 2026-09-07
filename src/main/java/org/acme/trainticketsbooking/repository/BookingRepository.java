package org.acme.trainticketsbooking.repository;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.trainticketsbooking.domain.CreatedBooking;
import org.acme.trainticketsbooking.domain.CreatedTicket;
import org.acme.trainticketsbooking.domain.SeatSelection;
import org.acme.trainticketsbooking.domain.TicketStatus;
import org.acme.trainticketsbooking.domain.TripCarriageSeatInfo;
import org.acme.trainticketsbooking.exception.DataAccessException;
import org.acme.trainticketsbooking.exception.SeatAlreadyTakenException;
import org.postgresql.util.PSQLException;
import org.postgresql.util.ServerErrorMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    private static final String FIND_TRIP_CARRIAGES_SQL = """
        SELECT t.id AS trip_id,
               tc.id AS trip_carriage_id,
               tc.number AS carriage_number,
               ct.seat_count
        FROM trip t
        LEFT JOIN trip_carriage tc ON tc.trip_id = t.id
        LEFT JOIN carriage_template ct ON ct.id = tc.carriage_template_id
        WHERE t.id = ?
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

    private final AgroalDataSource dataSource;

    public BookingRepository(AgroalDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Optional<List<TripCarriageSeatInfo>> findTripCarriages(long tripId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_TRIP_CARRIAGES_SQL)) {

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

    public Set<SeatKey> findActiveSeats(Collection<Long> tripCarriageIds) {
        if (tripCarriageIds == null || tripCarriageIds.isEmpty()) {
            return Set.of();
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ACTIVE_SEATS_SQL)) {

            statement.setArray(1, connection.createArrayOf("bigint", tripCarriageIds.toArray(Long[]::new)));
            try (ResultSet resultSet = statement.executeQuery()) {
                Set<SeatKey> occupied = new HashSet<>();
                while (resultSet.next()) {
                    occupied.add(new SeatKey(
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

    public CreatedBooking createBooking(List<SeatSelection> seats) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                UUID bookingId = insertBooking(connection);
                List<CreatedTicket> tickets = new ArrayList<>(seats.size());
                for (SeatSelection seat : seats) {
                    UUID ticketId = insertTicket(connection, bookingId, seat.tripCarriageId(), seat.seatNumber());
                    tickets.add(new CreatedTicket(
                        ticketId,
                        seat.carriageNumber(),
                        seat.seatNumber(),
                        TicketStatus.ACTIVE
                    ));
                }
                connection.commit();
                return new CreatedBooking(bookingId, tickets);
            } catch (SQLException e) {
                connection.rollback();
                if (isActiveSeatUniqueViolation(e)) {
                    throw new SeatAlreadyTakenException();
                }
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SeatAlreadyTakenException e) {
            throw e;
        } catch (SQLException e) {
            if (isActiveSeatUniqueViolation(e)) {
                throw new SeatAlreadyTakenException();
            }
            throw new DataAccessException("Ошибка при оформлении бронирования", e);
        }
    }

    private UUID insertBooking(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_BOOKING_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getObject("id", UUID.class);
        }
    }

    private UUID insertTicket(
            Connection connection,
            UUID bookingId,
            long tripCarriageId,
            short seatNumber
    ) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_TICKET_SQL)) {
            statement.setObject(1, bookingId);
            statement.setLong(2, tripCarriageId);
            statement.setShort(3, seatNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getObject("id", UUID.class);
            }
        }
    }

    private boolean isActiveSeatUniqueViolation(SQLException exception) {
        SQLException current = exception;
        while (current != null) {
            if (UNIQUE_VIOLATION.equals(current.getSQLState())) {
                if (current instanceof PSQLException psqlException) {
                    ServerErrorMessage serverError = psqlException.getServerErrorMessage();
                    if (serverError != null && "uq_active_ticket_seat".equals(serverError.getConstraint())) {
                        return true;
                    }
                }
                String message = current.getMessage();
                if (message != null && message.contains("uq_active_ticket_seat")) {
                    return true;
                }
            }
            current = current.getNextException();
        }
        return false;
    }

    public record SeatKey(long tripCarriageId, short seatNumber) {
    }
}
