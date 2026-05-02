package com.tourplanner.backend.service.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Ungültige Anmeldedaten");
    }
}
