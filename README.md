# Train Tickets Booking

REST API для поиска поездов, бронирования мест и отмены бронирований/билетов.

## Стек

Java 25, Quarkus 3.39.1, PostgreSQL 18, JDBC, Flyway, Swagger UI.

## Структура проекта

```
src/main/java/org/acme/trainticketsbooking/
├── resource/      # REST-контроллеры
├── service/       # бизнес-логика
├── repository/    # SQL через JDBC
├── dto/           # request / response
├── domain/        # модели и enum'ы
├── mapper/        # domain → dto
└── exception/     # исключения и HTTP-мапперы
src/main/resources/db/migration/
├── V1__init_schema.sql   # схема БД
└── R__seed_data.sql      # демо-данные
```

## Требования

Для запуска проекта достаточно установленного **Docker** с **Docker Compose**.

## Быстрый старт

```bash
git clone https://github.com/qelezy/train-tickets-booking.git
cd train-tickets-booking
cp .env.example .env
```

Заполните `.env` (пример):

```env
DB_USERNAME=user
DB_PASSWORD=password
DB_NAME=train_tickets
DB_PORT=5432
APP_PORT=8080
```

```bash
docker compose up
```

API: `http://localhost:${APP_PORT}`  
Swagger UI: `http://localhost:${APP_PORT}/q/swagger-ui`

## API Endpoints

Ошибки: `{ "status", "error", "message", "timestamp" }`

### Рейсы

| Метод | Эндпоинт | Описание |
| :--- | :--- | :--- |
| `GET` | `/api/v1/trips` | Поиск расписания (`from`, `to`, опционально `trainName`) |

<details>
<summary><b>Пример запроса и ответа для GET /api/v1/trips</b></summary>

**Запрос:**

```
GET /api/v1/trips?from=Липецк&to=Москва&trainName=Победа
```

**Ответ (200 OK):**

```json
[
  {
    "tripId": 7,
    "trainNumber": "737А",
    "trainName": "Победа",
    "departureStationName": "Липецк",
    "departureCity": "Липецк",
    "arrivalStationName": "Москва-Пассажирская-Казанская",
    "arrivalCity": "Москва",
    "departureTime": "2026-09-11T15:52:40.107746Z",
    "arrivalTime": "2026-09-11T22:42:40.107746Z",
    "availableSeats": [
      { "carriageType": "PLATSKART", "availableSeatsCount": 106 },
      { "carriageType": "COUPE", "availableSeatsCount": 72 }
    ]
  }
]
```

</details>

### Бронирования

| Метод | Эндпоинт | Описание |
| :--- | :--- | :--- |
| `POST` | `/api/v1/bookings` | Групповое бронирование мест |
| `POST` | `/api/v1/bookings/{bookingId}/cancel` | Отмена бронирования и всех активных билетов |

<details>
<summary><b>Пример запроса и ответа для POST /api/v1/bookings</b></summary>

**Запрос:**

```json
{
  "tripId": 1,
  "seats": [
    { "carriageNumber": 1, "seatNumber": 30 },
    { "carriageNumber": 1, "seatNumber": 31 }
  ]
}
```

**Ответ (201 Created):**

```json
{
  "bookingId": "<uuid>",
  "tickets": [
    { "ticketId": "<uuid>", "carriageNumber": 1, "seatNumber": 30, "status": "ACTIVE" },
    { "ticketId": "<uuid>", "carriageNumber": 1, "seatNumber": 31, "status": "ACTIVE" }
  ]
}
```

**Ответ (409 Conflict)** — место уже занято:

```json
{
  "status": 409,
  "error": "Конфликт",
  "message": "Место уже занято: вагон 1, место 30",
  "timestamp": "2026-09-11T15:26:29.834812513Z"
}
```
</details>

<details>
<summary><b>Пример ответа для POST /api/v1/bookings/{bookingId}/cancel</b></summary>

**Ответ (200 OK):**

```json
{
  "bookingId": "<uuid>",
  "status": "CANCELLED"
}
```

**Ответ (400 Bad Request)** — меньше чем за 2 часа до отправления:

```json
{
  "status": 400,
  "error": "Некорректный запрос",
  "message": "Отмена возможна не позднее чем за 2 часа до отправления",
  "timestamp": "2026-09-11T15:26:43.048701668Z"
}
```
</details>

### Билеты

| Метод | Эндпоинт | Описание |
| :--- | :--- | :--- |
| `POST` | `/api/v1/tickets/{ticketId}/cancel` | Отмена одного билета |

<details>
<summary><b>Пример ответа для POST /api/v1/tickets/{ticketId}/cancel</b></summary>

**Ответ (200 OK):**

```json
{
  "ticketId": "<uuid>",
  "status": "CANCELLED",
  "bookingId": "<uuid>",
  "bookingStatus": "CONFIRMED"
}
```

Если активных билетов не осталось, `bookingStatus` → `CANCELLED`.

**Ответ (400 Bad Request)** — меньше чем за 2 часа до отправления:

```json
{
  "status": 400,
  "error": "Некорректный запрос",
  "message": "Отмена возможна не позднее чем за 2 часа до отправления",
  "timestamp": "2026-09-11T15:26:43.106123742Z"
}
```
</details>

## Тесты

```bash
./mvnw test
```

Интеграционные тесты (Quarkus + REST Assured + Dev Services PostgreSQL): поиск, бронирование, конфликт мест, отмена.
