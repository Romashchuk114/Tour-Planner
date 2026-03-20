package com.tourplanner.backend.presentation.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TourLogRequestDTO {

    @NotNull
    private LocalDateTime dateTime;

    private String comment;

    @NotNull
    @Min(1)
    @Max(10)
    private Integer difficulty;

    @NotNull
    @Positive
    private Double totalDistance;

    @NotNull
    @Positive
    private Integer totalTime;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;
}