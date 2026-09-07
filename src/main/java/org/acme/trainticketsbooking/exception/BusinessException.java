package org.acme.trainticketsbooking.exception;

import jakarta.ws.rs.core.Response;

public class BusinessException extends RuntimeException {

    private final Response.Status status;
    private final String error;

    public BusinessException(Response.Status status, String error, String message) {
        super(message);
        this.status = status;
        this.error = error;
    }

    public Response.Status getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }
}
