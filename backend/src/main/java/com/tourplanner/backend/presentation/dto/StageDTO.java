package com.tourplanner.backend.presentation.dto;

import com.tourplanner.backend.model.TransportType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StageDTO(
        @NotNull TransportType transportType,
        @NotBlank String endName,
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double endLat,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double endLng
) {}
