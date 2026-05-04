package com.tourplanner.backend.presentation.dto;

public record StageResponseDTO(
        Integer orderIndex,
        String transportType,
        String endName,
        Double endLat,
        Double endLng,
        Double distance,
        Integer duration,
        String geometryGeoJson
) {}
