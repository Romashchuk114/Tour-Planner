package com.tourplanner.backend.presentation.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TourResponseDTO(
        Long id,
        Long userId,
        String name,
        String description,
        String fromName,
        Double fromLat,
        Double fromLng,
        Double totalDistance,
        Integer totalDuration,
        String tourImagePath,
        List<StageResponseDTO> stages,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
