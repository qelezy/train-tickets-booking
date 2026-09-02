CREATE TABLE station (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code VARCHAR(7) NOT NULL UNIQUE CHECK (code ~ '^[0-9]{7}$'),
    name VARCHAR(255) NOT NULL CHECK (trim(name) <> ''),
    city VARCHAR(100) NOT NULL CHECK (trim(city) <> '')
);

CREATE TABLE train (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    number VARCHAR(4) NOT NULL CHECK (number ~ '^[0-9]{3}[А-Я]?$') UNIQUE,
    name VARCHAR(100),
    departure_station_id BIGINT NOT NULL REFERENCES station(id) ON DELETE RESTRICT,
    arrival_station_id BIGINT NOT NULL REFERENCES station(id) ON DELETE RESTRICT,

    CONSTRAINT chk_train_stations CHECK (departure_station_id <> arrival_station_id)
);

CREATE TABLE carriage_template (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    type VARCHAR(20) NOT NULL CHECK (type IN ('LUX', 'SV', 'COUPE', 'PLATSKART', 'SEATING')),
    seat_count INTEGER NOT NULL CHECK (seat_count > 0)
);

CREATE TABLE trip (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    train_id BIGINT NOT NULL REFERENCES train(id) ON DELETE RESTRICT,
    departure_time TIMESTAMPTZ NOT NULL,
    arrival_time TIMESTAMPTZ NOT NULL,

    CONSTRAINT chk_trip_times CHECK (departure_time < arrival_time),
    CONSTRAINT uq_train_departure UNIQUE (train_id, departure_time)
);

CREATE TABLE trip_carriage (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    trip_id BIGINT NOT NULL REFERENCES trip(id) ON DELETE CASCADE,
    carriage_template_id BIGINT NOT NULL REFERENCES carriage_template(id) ON DELETE RESTRICT,
    number SMALLINT NOT NULL CHECK (number > 0),

    CONSTRAINT uq_trip_carriage_number UNIQUE (trip_id, number)
);

CREATE TABLE booking (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED' CHECK (status IN ('CONFIRMED', 'CANCELLED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    cancelled_at TIMESTAMPTZ,

    CONSTRAINT chk_booking_cancellation CHECK (
        (status = 'CONFIRMED' AND cancelled_at IS NULL) OR
        (status = 'CANCELLED' AND cancelled_at IS NOT NULL)
    )
);

CREATE TABLE ticket (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    booking_id UUID NOT NULL REFERENCES booking(id) ON DELETE RESTRICT,
    trip_carriage_id BIGINT NOT NULL REFERENCES trip_carriage(id) ON DELETE RESTRICT,
    seat_number SMALLINT NOT NULL CHECK (seat_number > 0),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'CANCELLED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    cancelled_at TIMESTAMPTZ,

    CONSTRAINT chk_ticket_cancellation CHECK (
        (status = 'ACTIVE' AND cancelled_at IS NULL) OR
        (status = 'CANCELLED' AND cancelled_at IS NOT NULL)
    )
);

CREATE UNIQUE INDEX uq_active_ticket_seat
    ON ticket (trip_carriage_id, seat_number)
    WHERE status = 'ACTIVE';

CREATE INDEX idx_station_city_lower
    ON station (LOWER(city));

CREATE INDEX idx_route_train_name_lower
    ON train (departure_station_id, arrival_station_id, LOWER(name));

CREATE INDEX idx_ticket_booking_id
    ON ticket (booking_id);
