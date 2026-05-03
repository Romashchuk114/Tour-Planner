package com.tourplanner.backend.presentation.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TourRequestDTO(
        @NotBlank String name,
        String description,
        @NotBlank String fromLocation,
        @NotBlank String toLocation,
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double fromLat,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double fromLng,
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double toLat,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double toLng,
        @NotBlank String transportType
) {}
