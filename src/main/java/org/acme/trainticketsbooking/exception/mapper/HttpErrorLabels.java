package org.acme.trainticketsbooking.exception.mapper;

final class HttpErrorLabels {

    private HttpErrorLabels() {
    }

    static String error(int statusCode) {
        return switch (statusCode) {
            case 400 -> "Некорректный запрос";
            case 404 -> "Не найдено";
            case 405 -> "Метод не поддерживается";
            case 409 -> "Конфликт";
            case 415 -> "Неподдерживаемый тип содержимого";
            case 500 -> "Внутренняя ошибка сервера";
            default -> "Ошибка запроса";
        };
    }

    static String defaultMessage(int statusCode) {
        return switch (statusCode) {
            case 400 -> "Некорректный запрос";
            case 404 -> "Ресурс не найден";
            case 405 -> "HTTP-метод не поддерживается для данного ресурса";
            case 409 -> "Конфликт данных";
            case 415 -> "Тип содержимого не поддерживается";
            case 500 -> "Внутренняя ошибка сервера";
            default -> "Произошла ошибка при обработке запроса";
        };
    }
}
