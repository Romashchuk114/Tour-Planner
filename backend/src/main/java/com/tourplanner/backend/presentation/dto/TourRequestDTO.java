package com.tourplanner.backend.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TourRequestDTO(
        @NotBlank String name,
        String description,
        @NotBlank String fromName,
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double fromLat,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double fromLng,
        @NotEmpty @Size(max = 9) @Valid List<StageDTO> stages
) {}
