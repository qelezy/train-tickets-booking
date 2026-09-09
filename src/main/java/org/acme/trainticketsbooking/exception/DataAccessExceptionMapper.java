package org.acme.trainticketsbooking.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.acme.trainticketsbooking.dto.response.ErrorResponse;
import org.jboss.logging.Logger;

@Provider
public class DataAccessExceptionMapper implements ExceptionMapper<DataAccessException> {

    private static final Logger LOG = Logger.getLogger(DataAccessExceptionMapper.class);

    @Override
    public Response toResponse(DataAccessException exception) {
        LOG.error("Database access error", exception);

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
