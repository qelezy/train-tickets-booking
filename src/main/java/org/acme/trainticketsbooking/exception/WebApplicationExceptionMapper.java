package org.acme.trainticketsbooking.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.acme.trainticketsbooking.dto.response.ErrorResponse;

@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    @Override
    public Response toResponse(WebApplicationException exception) {
        Response response = exception.getResponse();
        int statusCode = response.getStatus();
        String errorName = HttpErrorLabels.error(statusCode);
        String message = resolveMessage(exception, statusCode);

        ErrorResponse errorBody = new ErrorResponse(
            statusCode,
            errorName,
            message
        );

        return Response.status(statusCode)
            .entity(errorBody)
            .build();
    }

    private static String resolveMessage(WebApplicationException exception, int statusCode) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return HttpErrorLabels.defaultMessage(statusCode);
        }

        String reasonPhrase = exception.getResponse().getStatusInfo().getReasonPhrase();
        if (message.equals(reasonPhrase) || message.startsWith("HTTP ")) {
            return HttpErrorLabels.defaultMessage(statusCode);
        }

        return message;
    }
}
