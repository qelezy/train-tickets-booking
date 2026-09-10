package org.acme.trainticketsbooking.exception.common;

public class DataAccessException extends RuntimeException {
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
