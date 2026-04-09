package com.tourplanner.backend.presentation.dto;

import java.time.LocalDateTime;

public record TourLogResponseDTO(
        Long id,
        Long tourId,
        LocalDateTime dateTime,
        String comment,
        Integer difficulty,
        Double totalDistance,
        Integer totalTime,
        Integer rating,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
