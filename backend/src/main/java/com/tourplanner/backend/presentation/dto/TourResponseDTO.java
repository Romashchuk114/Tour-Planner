package com.tourplanner.backend.presentation.dto;

import com.tourplanner.backend.model.Tour;

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
        int logCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public TourResponseDTO(Tour tour, int logCount) {
        this(
                tour.getId(),
                tour.getUser().getId(),
                tour.getName(),
                tour.getDescription(),
                tour.getFromLocation(),
                tour.getToLocation(),
                tour.getTransportType().name(),
                tour.getTourDistance(),
                tour.getEstimatedTime(),
                tour.getTourImagePath(),
                logCount,
                tour.getCreatedAt(),
                tour.getUpdatedAt()
        );
    }
}
