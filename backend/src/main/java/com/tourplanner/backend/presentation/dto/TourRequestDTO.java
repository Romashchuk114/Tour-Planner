package com.tourplanner.backend.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record TourRequestDTO(
        @NotBlank String name,
        String description,
        @NotBlank String fromLocation,
        @NotBlank String toLocation,
        @NotBlank String transportType,
        @Positive Double tourDistance,
        @Positive Integer estimatedTime
) {}