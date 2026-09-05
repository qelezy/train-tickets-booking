package org.acme.trainticketsbooking.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.acme.trainticketsbooking.dto.response.ErrorResponse;

@Provider
public class DataAccessExceptionMapper implements ExceptionMapper<DataAccessException> {

    @Override
    public Response toResponse(DataAccessException exception) {
        int statusCode = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();

        ErrorResponse errorBody = new ErrorResponse(
            statusCode,
            "Ошибка базы данных",
            exception.getMessage()
        );

        return Response.status(statusCode)
            .entity(errorBody)
            .build();
    }
}
