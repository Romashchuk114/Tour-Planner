package com.tourplanner.backend.presentation.dto;

import com.tourplanner.backend.model.TourLog;

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
) {
    public TourLogResponseDTO(TourLog tourLog) {
        this(
                tourLog.getId(),
                tourLog.getTour().getId(),
                tourLog.getDateTime(),
                tourLog.getComment(),
                tourLog.getDifficulty(),
                tourLog.getTotalDistance(),
                tourLog.getTotalTime(),
                tourLog.getRating(),
                tourLog.getCreatedAt(),
                tourLog.getUpdatedAt()
        );
    }
}
