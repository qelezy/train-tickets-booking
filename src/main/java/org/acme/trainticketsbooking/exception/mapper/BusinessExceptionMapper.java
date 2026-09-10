package org.acme.trainticketsbooking.exception.mapper;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.acme.trainticketsbooking.dto.response.ErrorResponse;
import org.acme.trainticketsbooking.exception.common.BusinessException;

@Provider
public class BusinessExceptionMapper implements ExceptionMapper<BusinessException> {

    @Override
    public Response toResponse(BusinessException exception) {
        int statusCode = exception.getStatus().getStatusCode();

        ErrorResponse errorBody = new ErrorResponse(
            statusCode,
            exception.getError(),
            exception.getMessage()
        );

        return Response.status(statusCode)
            .entity(errorBody)
            .build();
    }
}
