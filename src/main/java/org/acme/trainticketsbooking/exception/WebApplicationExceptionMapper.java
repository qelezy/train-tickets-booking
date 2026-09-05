package org.acme.trainticketsbooking.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
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
        String errorName = response.getStatusInfo().getReasonPhrase();
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            message = errorName;
        }

        ErrorResponse errorBody = new ErrorResponse(
            statusCode,
            errorName,
            message
        );

        return Response.status(statusCode)
            .entity(errorBody)
            .build();
    }
}
