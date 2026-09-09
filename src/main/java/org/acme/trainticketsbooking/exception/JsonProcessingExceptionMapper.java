package org.acme.trainticketsbooking.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.acme.trainticketsbooking.dto.response.ErrorResponse;

@Provider
public class JsonProcessingExceptionMapper implements ExceptionMapper<JsonProcessingException> {

    @Override
    public Response toResponse(JsonProcessingException exception) {
        int statusCode = Response.Status.BAD_REQUEST.getStatusCode();
        ErrorResponse errorBody = new ErrorResponse(
            statusCode,
            HttpErrorLabels.error(statusCode),
            "Некорректный формат JSON в теле запроса"
        );

        return Response.status(statusCode)
            .entity(errorBody)
            .build();
    }
}
