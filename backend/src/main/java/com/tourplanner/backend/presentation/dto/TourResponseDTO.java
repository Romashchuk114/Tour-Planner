package com.tourplanner.backend.presentation.dto;

import java.time.LocalDateTime;

public record TourResponseDTO(
        Long id,
        Long userId,
        String name,
        String description,
        String fromLocation,
        String toLocation,
        String transportType,
        Double tourDistance,
        Integer estimatedTime,
        String tourImagePath,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
