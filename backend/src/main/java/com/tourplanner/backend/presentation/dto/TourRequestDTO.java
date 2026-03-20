package com.tourplanner.backend.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TourRequestDTO {

    @NotBlank
    private String name;

    private String description;

    @NotBlank
    private String fromLocation;

    @NotBlank
    private String toLocation;

    @NotBlank
    private String transportType;

    private Double tourDistance;

    private Integer estimatedTime;
}