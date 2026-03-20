package com.tourplanner.backend.presentation.dto;

import com.tourplanner.backend.business.TourLog;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TourLogResponseDTO {

    private final Long id;
    private final Long tourId;
    private final LocalDateTime dateTime;
    private final String comment;
    private final Integer difficulty;
    private final Double totalDistance;
    private final Integer totalTime;
    private final Integer rating;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public TourLogResponseDTO(TourLog tourLog) {
        this.id = tourLog.getId();
        this.tourId = tourLog.getTour().getId();
        this.dateTime = tourLog.getDateTime();
        this.comment = tourLog.getComment();
        this.difficulty = tourLog.getDifficulty();
        this.totalDistance = tourLog.getTotalDistance();
        this.totalTime = tourLog.getTotalTime();
        this.rating = tourLog.getRating();
        this.createdAt = tourLog.getCreatedAt();
        this.updatedAt = tourLog.getUpdatedAt();
    }
}