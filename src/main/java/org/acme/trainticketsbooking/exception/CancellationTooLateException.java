package org.acme.trainticketsbooking.exception;

import jakarta.ws.rs.core.Response;

public class CancellationTooLateException extends BusinessException {

    public CancellationTooLateException() {
        super(
            Response.Status.BAD_REQUEST,
            "Некорректный запрос",
            "Отмена возможна не позднее чем за 2 часа до отправления"
        );
    }
}
