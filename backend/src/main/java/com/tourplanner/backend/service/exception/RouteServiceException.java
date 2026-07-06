package com.tourplanner.backend.service.exception;

public class RouteServiceException extends RuntimeException {
    public RouteServiceException(String message) {
        super(message);
    }

    public RouteServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
