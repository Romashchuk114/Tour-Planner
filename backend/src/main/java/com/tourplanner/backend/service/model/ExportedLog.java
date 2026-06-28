package com.tourplanner.backend.service.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record ExportedLog(
        @NotNull LocalDateTime dateTime,
        String comment,
        @NotNull @Min(1) @Max(10) Integer difficulty,
        @NotNull @Positive Double totalDistance,
        @NotNull @Positive Integer totalTime,
        @NotNull @Min(1) @Max(5) Integer rating
) {}
