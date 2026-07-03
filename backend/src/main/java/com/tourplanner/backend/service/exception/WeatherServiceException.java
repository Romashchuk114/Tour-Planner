package com.tourplanner.backend.service.exception;

public class WeatherServiceException extends RuntimeException {

    public WeatherServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
