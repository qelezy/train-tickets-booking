TRUNCATE TABLE ticket, booking, trip_carriage, trip, train, carriage_template, station
    RESTART IDENTITY CASCADE;

INSERT INTO station (code, name, city) VALUES
    ('2014130', 'Липецк', 'Липецк'),
    ('2000003', 'Москва-Пассажирская-Казанская', 'Москва'),
    ('2000005', 'Москва-Пассажирская-Павелецкая', 'Москва'),
    ('2006004', 'Москва-Пассажирская', 'Москва'),
    ('2014001', 'Воронеж-1', 'Воронеж'),
    ('2004001', 'Санкт-Петербург-Главный', 'Санкт-Петербург');

INSERT INTO carriage_template (type, seat_count) VALUES
    ('LUX', 18),
    ('SV', 18),
    ('COUPE', 36),
    ('PLATSKART', 54),
    ('SEATING', 81);

INSERT INTO train (number, name, departure_station_id, arrival_station_id) VALUES
    ('737А', 'Победа',
        (SELECT id FROM station WHERE code = '2014130'),
        (SELECT id FROM station WHERE code = '2000003')),
    ('738А', 'Победа',
        (SELECT id FROM station WHERE code = '2000003'),
        (SELECT id FROM station WHERE code = '2014130')),
    ('025В', 'Воронеж',
        (SELECT id FROM station WHERE code = '2014001'),
        (SELECT id FROM station WHERE code = '2000005')),
    ('025Я', 'Воронеж',
        (SELECT id FROM station WHERE code = '2000005'),
        (SELECT id FROM station WHERE code = '2014001')),
    ('752А', 'Сапсан',
        (SELECT id FROM station WHERE code = '2006004'),
        (SELECT id FROM station WHERE code = '2004001')),
    ('751А', 'Сапсан',
        (SELECT id FROM station WHERE code = '2004001'),
        (SELECT id FROM station WHERE code = '2006004'));

INSERT INTO trip (train_id, departure_time, arrival_time)
SELECT t.id, v.departure_time, v.arrival_time
FROM train t
JOIN (
    VALUES
        ('737А', NOW() + INTERVAL '30 minutes', NOW() + INTERVAL '30 minutes' + INTERVAL '6 hours 50 minutes'),
        ('737А', NOW() + INTERVAL '1 day',   NOW() + INTERVAL '1 day'   + INTERVAL '6 hours 50 minutes'),
        ('737А', NOW() + INTERVAL '2 days',  NOW() + INTERVAL '2 days'  + INTERVAL '6 hours 50 minutes'),
        ('737А', NOW() + INTERVAL '3 days',  NOW() + INTERVAL '3 days'  + INTERVAL '6 hours 50 minutes'),
        ('737А', NOW() + INTERVAL '5 days',  NOW() + INTERVAL '5 days'  + INTERVAL '6 hours 50 minutes'),
        ('737А', NOW() + INTERVAL '8 days',  NOW() + INTERVAL '8 days'  + INTERVAL '6 hours 50 minutes'),
        ('737А', NOW() + INTERVAL '10 days', NOW() + INTERVAL '10 days' + INTERVAL '6 hours 50 minutes'),
        ('738А', NOW() + INTERVAL '2 days',  NOW() + INTERVAL '2 days'  + INTERVAL '7 hours'),
        ('738А', NOW() + INTERVAL '4 days',  NOW() + INTERVAL '4 days'  + INTERVAL '7 hours'),
        ('738А', NOW() + INTERVAL '6 days',  NOW() + INTERVAL '6 days'  + INTERVAL '7 hours'),
        ('738А', NOW() + INTERVAL '9 days',  NOW() + INTERVAL '9 days'  + INTERVAL '7 hours'),
        ('025В', NOW() + INTERVAL '2 days',  NOW() + INTERVAL '2 days'  + INTERVAL '10 hours 50 minutes'),
        ('025В', NOW() + INTERVAL '5 days',  NOW() + INTERVAL '5 days'  + INTERVAL '10 hours 50 minutes'),
        ('025В', NOW() + INTERVAL '8 days',  NOW() + INTERVAL '8 days'  + INTERVAL '10 hours 50 minutes'),
        ('025Я', NOW() + INTERVAL '1 day',   NOW() + INTERVAL '1 day'   + INTERVAL '11 hours 20 minutes'),
        ('025Я', NOW() + INTERVAL '4 days',  NOW() + INTERVAL '4 days'  + INTERVAL '11 hours 20 minutes'),
        ('025Я', NOW() + INTERVAL '7 days',  NOW() + INTERVAL '7 days'  + INTERVAL '11 hours 20 minutes'),
        ('752А', NOW() + INTERVAL '1 day',   NOW() + INTERVAL '1 day'   + INTERVAL '3 hours 50 minutes'),
        ('752А', NOW() + INTERVAL '3 days',  NOW() + INTERVAL '3 days'  + INTERVAL '3 hours 50 minutes'),
        ('752А', NOW() + INTERVAL '7 days',  NOW() + INTERVAL '7 days'  + INTERVAL '3 hours 50 minutes'),
        ('751А', NOW() + INTERVAL '2 days',  NOW() + INTERVAL '2 days'  + INTERVAL '3 hours 55 minutes'),
        ('751А', NOW() + INTERVAL '6 days',  NOW() + INTERVAL '6 days'  + INTERVAL '3 hours 55 minutes')
) AS v(train_number, departure_time, arrival_time) ON t.number = v.train_number;

INSERT INTO trip_carriage (trip_id, carriage_template_id, number)
SELECT tr.id, ct.id, c.carriage_number
FROM trip tr
JOIN train t ON t.id = tr.train_id
CROSS JOIN (
    VALUES
        (1::smallint, 'PLATSKART'),
        (2::smallint, 'PLATSKART'),
        (3::smallint, 'COUPE'),
        (4::smallint, 'COUPE'),
        (5::smallint, 'SV'),
        (6::smallint, 'LUX')
) AS c(carriage_number, carriage_type)
JOIN LATERAL (
    SELECT id
    FROM carriage_template
    WHERE type = c.carriage_type
    ORDER BY id
    LIMIT 1
) ct ON TRUE
WHERE t.number IN ('737А', '738А');

INSERT INTO trip_carriage (trip_id, carriage_template_id, number)
SELECT tr.id, ct.id, c.carriage_number
FROM trip tr
JOIN train t ON t.id = tr.train_id
CROSS JOIN (
    VALUES
        (1::smallint, 'SEATING'),
        (2::smallint, 'SEATING'),
        (3::smallint, 'SEATING'),
        (4::smallint, 'SEATING')
) AS c(carriage_number, carriage_type)
JOIN LATERAL (
    SELECT id
    FROM carriage_template
    WHERE type = c.carriage_type
    ORDER BY id
    LIMIT 1
) ct ON TRUE
WHERE t.number IN ('752А', '751А');

INSERT INTO trip_carriage (trip_id, carriage_template_id, number)
SELECT tr.id, ct.id, c.carriage_number
FROM trip tr
JOIN train t ON t.id = tr.train_id
CROSS JOIN (
    VALUES
        (1::smallint, 'PLATSKART'),
        (2::smallint, 'PLATSKART'),
        (3::smallint, 'COUPE'),
        (4::smallint, 'SV')
) AS c(carriage_number, carriage_type)
JOIN LATERAL (
    SELECT id
    FROM carriage_template
    WHERE type = c.carriage_type
    ORDER BY id
    LIMIT 1
) ct ON TRUE
WHERE t.number IN ('025В', '025Я');

DO $$
DECLARE
    selected_trip_id BIGINT;
    selected_booking_id UUID;
    selected_carriage_id BIGINT;
BEGIN
    SELECT tr.id INTO STRICT selected_trip_id
    FROM trip tr
    JOIN train t ON t.id = tr.train_id
    WHERE t.number = '737А'
      AND tr.departure_time > NOW()
      AND tr.departure_time < NOW() + INTERVAL '2 hours'
    ORDER BY tr.departure_time
    LIMIT 1;

    INSERT INTO booking (status) VALUES ('CONFIRMED') RETURNING id INTO selected_booking_id;
    SELECT id INTO STRICT selected_carriage_id
    FROM trip_carriage WHERE trip_id = selected_trip_id AND number = 1;
    INSERT INTO ticket (booking_id, trip_carriage_id, seat_number, status) VALUES
        (selected_booking_id, selected_carriage_id, 1, 'ACTIVE'),
        (selected_booking_id, selected_carriage_id, 2, 'ACTIVE');

    SELECT tr.id INTO STRICT selected_trip_id
    FROM trip tr
    JOIN train t ON t.id = tr.train_id
    WHERE t.number = '737А' AND tr.departure_time > NOW() + INTERVAL '12 hours'
    ORDER BY tr.departure_time
    LIMIT 1;

    INSERT INTO booking (status) VALUES ('CONFIRMED') RETURNING id INTO selected_booking_id;
    SELECT id INTO STRICT selected_carriage_id
    FROM trip_carriage WHERE trip_id = selected_trip_id AND number = 1;
    INSERT INTO ticket (booking_id, trip_carriage_id, seat_number, status) VALUES
        (selected_booking_id, selected_carriage_id, 1, 'ACTIVE'),
        (selected_booking_id, selected_carriage_id, 2, 'ACTIVE');

    INSERT INTO booking (status) VALUES ('CONFIRMED') RETURNING id INTO selected_booking_id;
    SELECT id INTO STRICT selected_carriage_id
    FROM trip_carriage WHERE trip_id = selected_trip_id AND number = 3;
    INSERT INTO ticket (booking_id, trip_carriage_id, seat_number, status) VALUES
        (selected_booking_id, selected_carriage_id, 5, 'ACTIVE'),
        (selected_booking_id, selected_carriage_id, 6, 'ACTIVE');

    INSERT INTO booking (status) VALUES ('CONFIRMED') RETURNING id INTO selected_booking_id;
    SELECT id INTO STRICT selected_carriage_id
    FROM trip_carriage WHERE trip_id = selected_trip_id AND number = 2;
    INSERT INTO ticket (booking_id, trip_carriage_id, seat_number, status) VALUES
        (selected_booking_id, selected_carriage_id, 10, 'ACTIVE'),
        (selected_booking_id, selected_carriage_id, 11, 'ACTIVE');

    INSERT INTO booking (status, cancelled_at)
    VALUES ('CANCELLED', NOW() - INTERVAL '2 days')
    RETURNING id INTO selected_booking_id;
    SELECT id INTO STRICT selected_carriage_id
    FROM trip_carriage WHERE trip_id = selected_trip_id AND number = 5;
    INSERT INTO ticket (booking_id, trip_carriage_id, seat_number, status, cancelled_at) VALUES
        (selected_booking_id, selected_carriage_id, 1, 'CANCELLED', NOW() - INTERVAL '2 days');

    SELECT tr.id INTO STRICT selected_trip_id
    FROM trip tr
    JOIN train t ON t.id = tr.train_id
    WHERE t.number = '737А' AND tr.departure_time > NOW() + INTERVAL '12 hours'
    ORDER BY tr.departure_time
    OFFSET 1 LIMIT 1;

    INSERT INTO booking (status) VALUES ('CONFIRMED') RETURNING id INTO selected_booking_id;
    SELECT id INTO STRICT selected_carriage_id
    FROM trip_carriage WHERE trip_id = selected_trip_id AND number = 1;
    INSERT INTO ticket (booking_id, trip_carriage_id, seat_number, status) VALUES
        (selected_booking_id, selected_carriage_id, 15, 'ACTIVE'),
        (selected_booking_id, selected_carriage_id, 16, 'ACTIVE');

    INSERT INTO booking (status) VALUES ('CONFIRMED') RETURNING id INTO selected_booking_id;
    SELECT id INTO STRICT selected_carriage_id
    FROM trip_carriage WHERE trip_id = selected_trip_id AND number = 3;
    INSERT INTO ticket (booking_id, trip_carriage_id, seat_number, status) VALUES
        (selected_booking_id, selected_carriage_id, 8, 'ACTIVE'),
        (selected_booking_id, selected_carriage_id, 9, 'ACTIVE');

    INSERT INTO booking (status) VALUES ('CONFIRMED') RETURNING id INTO selected_booking_id;
    SELECT id INTO STRICT selected_carriage_id
    FROM trip_carriage WHERE trip_id = selected_trip_id AND number = 6;
    INSERT INTO ticket (booking_id, trip_carriage_id, seat_number, status) VALUES
        (selected_booking_id, selected_carriage_id, 3, 'ACTIVE'),
        (selected_booking_id, selected_carriage_id, 4, 'ACTIVE');

    SELECT tr.id INTO STRICT selected_trip_id
    FROM trip tr
    JOIN train t ON t.id = tr.train_id
    WHERE t.number = '737А' AND tr.departure_time > NOW() + INTERVAL '12 hours'
    ORDER BY tr.departure_time
    OFFSET 2 LIMIT 1;

    INSERT INTO booking (status) VALUES ('CONFIRMED') RETURNING id INTO selected_booking_id;
    SELECT id INTO STRICT selected_carriage_id
    FROM trip_carriage WHERE trip_id = selected_trip_id AND number = 2;
    INSERT INTO ticket (booking_id, trip_carriage_id, seat_number, status) VALUES
        (selected_booking_id, selected_carriage_id, 20, 'ACTIVE'),
        (selected_booking_id, selected_carriage_id, 21, 'ACTIVE'),
        (selected_booking_id, selected_carriage_id, 22, 'ACTIVE');

    SELECT tr.id INTO STRICT selected_trip_id
    FROM trip tr
    JOIN train t ON t.id = tr.train_id
    WHERE t.number = '737А' AND tr.departure_time > NOW() + INTERVAL '12 hours'
    ORDER BY tr.departure_time
    OFFSET 3 LIMIT 1;

    INSERT INTO booking (status, cancelled_at)
    VALUES ('CANCELLED', NOW() - INTERVAL '1 day')
    RETURNING id INTO selected_booking_id;
    SELECT id INTO STRICT selected_carriage_id
    FROM trip_carriage WHERE trip_id = selected_trip_id AND number = 1;
    INSERT INTO ticket (booking_id, trip_carriage_id, seat_number, status, cancelled_at) VALUES
        (selected_booking_id, selected_carriage_id, 3, 'CANCELLED', NOW() - INTERVAL '1 day');

    INSERT INTO booking (status, cancelled_at)
    VALUES ('CANCELLED', NOW() - INTERVAL '1 day')
    RETURNING id INTO selected_booking_id;
    SELECT id INTO STRICT selected_carriage_id
    FROM trip_carriage WHERE trip_id = selected_trip_id AND number = 2;
    INSERT INTO ticket (booking_id, trip_carriage_id, seat_number, status, cancelled_at) VALUES
        (selected_booking_id, selected_carriage_id, 20, 'CANCELLED', NOW() - INTERVAL '1 day');

    INSERT INTO booking (status) VALUES ('CONFIRMED') RETURNING id INTO selected_booking_id;
    SELECT id INTO STRICT selected_carriage_id
    FROM trip_carriage WHERE trip_id = selected_trip_id AND number = 3;
    INSERT INTO ticket (booking_id, trip_carriage_id, seat_number, status) VALUES
        (selected_booking_id, selected_carriage_id, 12, 'ACTIVE');

    INSERT INTO booking (status) VALUES ('CONFIRMED') RETURNING id INTO selected_booking_id;
    SELECT id INTO STRICT selected_carriage_id
    FROM trip_carriage WHERE trip_id = selected_trip_id AND number = 4;
    INSERT INTO ticket (booking_id, trip_carriage_id, seat_number, status) VALUES
        (selected_booking_id, selected_carriage_id, 7, 'ACTIVE');

    SELECT tr.id INTO STRICT selected_trip_id
    FROM trip tr
    JOIN train t ON t.id = tr.train_id
    WHERE t.number = '738А' AND tr.departure_time > NOW()
    ORDER BY tr.departure_time
    LIMIT 1;

    INSERT INTO booking (status) VALUES ('CONFIRMED') RETURNING id INTO selected_booking_id;
    SELECT id INTO STRICT selected_carriage_id
    FROM trip_carriage WHERE trip_id = selected_trip_id AND number = 1;
    INSERT INTO ticket (booking_id, trip_carriage_id, seat_number, status) VALUES
        (selected_booking_id, selected_carriage_id, 7, 'ACTIVE'),
        (selected_booking_id, selected_carriage_id, 8, 'ACTIVE');

    INSERT INTO booking (status) VALUES ('CONFIRMED') RETURNING id INTO selected_booking_id;
    SELECT id INTO STRICT selected_carriage_id
    FROM trip_carriage WHERE trip_id = selected_trip_id AND number = 3;
    INSERT INTO ticket (booking_id, trip_carriage_id, seat_number, status) VALUES
        (selected_booking_id, selected_carriage_id, 14, 'ACTIVE');

    SELECT tr.id INTO STRICT selected_trip_id
    FROM trip tr
    JOIN train t ON t.id = tr.train_id
    WHERE t.number = '752А' AND tr.departure_time > NOW()
    ORDER BY tr.departure_time
    LIMIT 1;

    INSERT INTO booking (status) VALUES ('CONFIRMED') RETURNING id INTO selected_booking_id;
    SELECT id INTO STRICT selected_carriage_id
    FROM trip_carriage WHERE trip_id = selected_trip_id AND number = 1;
    INSERT INTO ticket (booking_id, trip_carriage_id, seat_number, status) VALUES
        (selected_booking_id, selected_carriage_id, 2, 'ACTIVE');

    INSERT INTO booking (status) VALUES ('CONFIRMED') RETURNING id INTO selected_booking_id;
    SELECT id INTO STRICT selected_carriage_id
    FROM trip_carriage WHERE trip_id = selected_trip_id AND number = 3;
    INSERT INTO ticket (booking_id, trip_carriage_id, seat_number, status) VALUES
        (selected_booking_id, selected_carriage_id, 15, 'ACTIVE'),
        (selected_booking_id, selected_carriage_id, 16, 'ACTIVE');

    SELECT tr.id INTO STRICT selected_trip_id
    FROM trip tr
    JOIN train t ON t.id = tr.train_id
    WHERE t.number = '751А' AND tr.departure_time > NOW()
    ORDER BY tr.departure_time
    LIMIT 1;

    INSERT INTO booking (status) VALUES ('CONFIRMED') RETURNING id INTO selected_booking_id;
    SELECT id INTO STRICT selected_carriage_id
    FROM trip_carriage WHERE trip_id = selected_trip_id AND number = 2;
    INSERT INTO ticket (booking_id, trip_carriage_id, seat_number, status) VALUES
        (selected_booking_id, selected_carriage_id, 21, 'ACTIVE');

    INSERT INTO booking (status) VALUES ('CONFIRMED') RETURNING id INTO selected_booking_id;
    SELECT id INTO STRICT selected_carriage_id
    FROM trip_carriage WHERE trip_id = selected_trip_id AND number = 4;
    INSERT INTO ticket (booking_id, trip_carriage_id, seat_number, status) VALUES
        (selected_booking_id, selected_carriage_id, 6, 'ACTIVE');

    SELECT tr.id INTO STRICT selected_trip_id
    FROM trip tr
    JOIN train t ON t.id = tr.train_id
    WHERE t.number = '025В' AND tr.departure_time > NOW()
    ORDER BY tr.departure_time
    LIMIT 1;

    INSERT INTO booking (status) VALUES ('CONFIRMED') RETURNING id INTO selected_booking_id;
    SELECT id INTO STRICT selected_carriage_id
    FROM trip_carriage WHERE trip_id = selected_trip_id AND number = 1;
    INSERT INTO ticket (booking_id, trip_carriage_id, seat_number, status) VALUES
        (selected_booking_id, selected_carriage_id, 30, 'ACTIVE');

    INSERT INTO booking (status) VALUES ('CONFIRMED') RETURNING id INTO selected_booking_id;
    SELECT id INTO STRICT selected_carriage_id
    FROM trip_carriage WHERE trip_id = selected_trip_id AND number = 3;
    INSERT INTO ticket (booking_id, trip_carriage_id, seat_number, status) VALUES
        (selected_booking_id, selected_carriage_id, 4, 'ACTIVE'),
        (selected_booking_id, selected_carriage_id, 5, 'ACTIVE');

    SELECT tr.id INTO STRICT selected_trip_id
    FROM trip tr
    JOIN train t ON t.id = tr.train_id
    WHERE t.number = '025Я' AND tr.departure_time > NOW()
    ORDER BY tr.departure_time
    LIMIT 1;

    INSERT INTO booking (status) VALUES ('CONFIRMED') RETURNING id INTO selected_booking_id;
    SELECT id INTO STRICT selected_carriage_id
    FROM trip_carriage WHERE trip_id = selected_trip_id AND number = 2;
    INSERT INTO ticket (booking_id, trip_carriage_id, seat_number, status) VALUES
        (selected_booking_id, selected_carriage_id, 18, 'ACTIVE');

    INSERT INTO booking (status) VALUES ('CONFIRMED') RETURNING id INTO selected_booking_id;
    SELECT id INTO STRICT selected_carriage_id
    FROM trip_carriage WHERE trip_id = selected_trip_id AND number = 4;
    INSERT INTO ticket (booking_id, trip_carriage_id, seat_number, status) VALUES
        (selected_booking_id, selected_carriage_id, 2, 'ACTIVE');
END $$;
