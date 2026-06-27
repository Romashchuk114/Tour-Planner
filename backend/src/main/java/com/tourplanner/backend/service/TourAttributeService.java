package com.tourplanner.backend.service;

import com.tourplanner.backend.data.TourLogAggregate;
import com.tourplanner.backend.data.TourLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TourAttributeService {

    private final TourLogRepository tourLogRepository;

    @Transactional(readOnly = true)
    public Map<Long, ComputedAttributes> computeForUser(Long userId) {
        return tourLogRepository.findAggregatesByUserId(userId).stream()
                .collect(Collectors.toMap(TourLogAggregate::getTourId, this::toAttributes));
    }

    @Transactional(readOnly = true)
    public ComputedAttributes computeForTour(Long tourId) {
        return tourLogRepository.findAggregateByTourId(tourId)
                .map(this::toAttributes)
                .orElse(ComputedAttributes.NONE);
    }

    private ComputedAttributes toAttributes(TourLogAggregate aggregate) {
        return new ComputedAttributes(
                aggregate.getLogCount(),
                popularityLabel(aggregate.getLogCount()),
                childFriendlinessLabel(aggregate.getAvgDifficulty(), aggregate.getAvgTime(), aggregate.getAvgDistance())
        );
    }

    private String popularityLabel(long logCount) {
        if (logCount >= 10) return "Sehr hoch";
        if (logCount >= 6) return "Hoch";
        if (logCount >= 3) return "Mittel";
        if (logCount >= 1) return "Niedrig";
        return "Keine";
    }

    private String childFriendlinessLabel(Double avgDifficulty, Double avgTimeMinutes, Double avgDistanceKm) {
        if (avgDifficulty == null || avgTimeMinutes == null || avgDistanceKm == null) {
            return "Unbekannt";
        }

        int score = 0;
        if (avgDifficulty <= 3) score += 2;
        else if (avgDifficulty <= 5) score += 1;

        if (avgTimeMinutes <= 120) score += 2;
        else if (avgTimeMinutes <= 240) score += 1;

        if (avgDistanceKm <= 5) score += 2;
        else if (avgDistanceKm <= 15) score += 1;

        if (score >= 6) return "Sehr gut geeignet";
        if (score >= 4) return "Gut geeignet";
        if (score >= 2) return "Bedingt geeignet";
        return "Nicht geeignet";
    }
}
