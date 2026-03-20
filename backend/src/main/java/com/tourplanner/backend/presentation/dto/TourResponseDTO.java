package com.tourplanner.backend.presentation.dto;

import com.tourplanner.backend.business.Tour;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TourResponseDTO {

    private final Long id;
    private final Long userId;
    private final String name;
    private final String description;
    private final String fromLocation;
    private final String toLocation;
    private final String transportType;
    private final Double tourDistance;
    private final Integer estimatedTime;
    private final String tourImagePath;
    private final int logCount;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public TourResponseDTO(Tour tour, int logCount) {
        this.id = tour.getId();
        this.userId = tour.getUser().getId();
        this.name = tour.getName();
        this.description = tour.getDescription();
        this.fromLocation = tour.getFromLocation();
        this.toLocation = tour.getToLocation();
        this.transportType = tour.getTransportType().name();
        this.tourDistance = tour.getTourDistance();
        this.estimatedTime = tour.getEstimatedTime();
        this.tourImagePath = tour.getTourImagePath();
        this.logCount = logCount;
        this.createdAt = tour.getCreatedAt();
        this.updatedAt = tour.getUpdatedAt();
    }
}