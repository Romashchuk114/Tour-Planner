package com.tourplanner.backend.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record TourRequestDTO(
        @NotBlank String name,
        String description,
        @NotBlank String fromLocation,
        @NotBlank String toLocation,
        @NotBlank String transportType,
        Double tourDistance,
        Integer estimatedTime
) {}