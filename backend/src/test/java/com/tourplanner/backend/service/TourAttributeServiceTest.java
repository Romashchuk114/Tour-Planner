package com.tourplanner.backend.service;

import com.tourplanner.backend.data.TourLogAggregate;
import com.tourplanner.backend.data.TourLogRepository;
import com.tourplanner.backend.service.model.ComputedAttributes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TourAttributeServiceTest {

    @Mock private TourLogRepository tourLogRepository;

    @InjectMocks
    private TourAttributeService tourAttributeService;

    private TourLogAggregate aggregate(long tourId, long logCount, Double avgDifficulty, Double avgTime, Double avgDistance) {
        return new TourLogAggregate() {
            @Override public Long getTourId() { return tourId; }
            @Override public long getLogCount() { return logCount; }
            @Override public Double getAvgDifficulty() { return avgDifficulty; }
            @Override public Double getAvgTime() { return avgTime; }
            @Override public Double getAvgDistance() { return avgDistance; }
        };
    }

    private ComputedAttributes forAggregate(TourLogAggregate aggregate) {
        when(tourLogRepository.findAggregateByTourId(1L)).thenReturn(Optional.of(aggregate));
        return tourAttributeService.computeForTour(1L);
    }

    @Test
    void tourWithoutLogsGetsDefaultAttributes() {
        when(tourLogRepository.findAggregateByTourId(1L)).thenReturn(Optional.empty());

        ComputedAttributes attributes = tourAttributeService.computeForTour(1L);

        assertEquals(0, attributes.logCount());
        assertEquals("Keine", attributes.popularity());
        assertEquals("Unbekannt", attributes.childFriendliness());
    }

    @Test
    void popularityIsDerivedFromLogCount() {
        assertEquals("Niedrig", forAggregate(aggregate(1L, 1, 5.0, 60.0, 5.0)).popularity());
        assertEquals("Mittel", forAggregate(aggregate(1L, 3, 5.0, 60.0, 5.0)).popularity());
        assertEquals("Hoch", forAggregate(aggregate(1L, 6, 5.0, 60.0, 5.0)).popularity());
        assertEquals("Sehr hoch", forAggregate(aggregate(1L, 10, 5.0, 60.0, 5.0)).popularity());
    }

    @Test
    void easyShortTourIsVeryChildFriendly() {
        ComputedAttributes attributes = forAggregate(aggregate(1L, 2, 2.0, 60.0, 3.0));

        assertEquals("Sehr gut geeignet", attributes.childFriendliness());
    }

    @Test
    void hardLongTourIsNotChildFriendly() {
        ComputedAttributes attributes = forAggregate(aggregate(1L, 2, 9.0, 600.0, 80.0));

        assertEquals("Nicht geeignet", attributes.childFriendliness());
    }

    @Test
    void computeForUserGroupsAttributesByTourId() {
        when(tourLogRepository.findAggregatesByUserId(1L)).thenReturn(List.of(
                aggregate(10L, 1, 2.0, 60.0, 3.0),
                aggregate(20L, 10, 9.0, 600.0, 80.0)
        ));

        Map<Long, ComputedAttributes> attributes = tourAttributeService.computeForUser(1L);

        assertEquals(2, attributes.size());
        assertEquals("Niedrig", attributes.get(10L).popularity());
        assertEquals("Sehr hoch", attributes.get(20L).popularity());
    }
}
