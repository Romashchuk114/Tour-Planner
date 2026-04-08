package com.tourplanner.backend.presentation.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record TourLogRequestDTO(
        @NotNull LocalDateTime dateTime,
        String comment,
        @NotNull @Min(1) @Max(10) Integer difficulty,
        @NotNull @Positive Double totalDistance,
        @NotNull @Positive Integer totalTime,
        @NotNull @Min(1) @Max(5) Integer rating
) {}
