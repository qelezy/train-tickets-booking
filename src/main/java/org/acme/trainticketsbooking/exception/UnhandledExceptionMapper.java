package org.acme.trainticketsbooking.exception;

import io.quarkus.logging.Log;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.acme.trainticketsbooking.dto.response.ErrorResponse;

@Provider
public class UnhandledExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        Log.error("Необработанная ошибка сервера", exception);

        int statusCode = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        ErrorResponse errorBody = new ErrorResponse(
            statusCode,
            HttpErrorLabels.error(statusCode),
            HttpErrorLabels.defaultMessage(statusCode)
        );

        return Response.status(statusCode)
            .entity(errorBody)
            .build();
    }
}
