package com.tourplanner.backend.presentation.dto;

import java.time.LocalDateTime;

public record TourResponseDTO(
        Long id,
        Long userId,
        String name,
        String description,
        String fromLocation,
        String toLocation,
        Double fromLat,
        Double fromLng,
        Double toLat,
        Double toLng,
        String transportType,
        Double tourDistance,
        Integer estimatedTime,
        String tourImagePath,
        String routeGeometry,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
