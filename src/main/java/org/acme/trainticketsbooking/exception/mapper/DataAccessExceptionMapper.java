package org.acme.trainticketsbooking.exception.mapper;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import io.quarkus.logging.Log;
import org.acme.trainticketsbooking.dto.response.ErrorResponse;
import org.acme.trainticketsbooking.exception.common.DataAccessException;

@Provider
public class DataAccessExceptionMapper implements ExceptionMapper<DataAccessException> {

    @Override
    public Response toResponse(DataAccessException exception) {
        Log.error("Ошибка доступа к базе данных", exception);

        int statusCode = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        ErrorResponse errorBody = new ErrorResponse(
            statusCode,
            "Ошибка базы данных",
            "Внутренняя ошибка сервера"
        );

        return Response.status(statusCode)
            .entity(errorBody)
            .build();
    }
}
