package com.ansh.common.exception;

/**
 * Exception thrown for bad request errors (invalid input, validation failures, business logic violations).
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
