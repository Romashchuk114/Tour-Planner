package com.tourplanner.backend.service;

import com.tourplanner.backend.data.TourLogRepository;
import com.tourplanner.backend.data.TourRepository;
import com.tourplanner.backend.model.Tour;
import com.tourplanner.backend.model.TourLog;
import com.tourplanner.backend.model.User;
import com.tourplanner.backend.service.exception.ForbiddenException;
import com.tourplanner.backend.service.model.TourLogRequestParams;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TourLogServiceTest {

    @Mock private TourLogRepository tourLogRepository;
    @Mock private TourRepository tourRepository;

    @InjectMocks
    private TourLogService tourLogService;

    private Tour tour(long id, long userId) {
        User user = new User();
        user.setId(userId);
        Tour tour = new Tour();
        tour.setId(id);
        tour.setUser(user);
        return tour;
    }

    @Test
    void createMapsAllFieldsToLog() {
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour(1L, 1L)));
        when(tourLogRepository.save(any(TourLog.class))).thenAnswer(inv -> inv.getArgument(0));
        LocalDateTime dateTime = LocalDateTime.of(2026, 7, 1, 10, 30);

        TourLog created = tourLogService.create(1L, 1L,
                new TourLogRequestParams(dateTime, "Super Tour", 4, 12.5, 90, 5));

        assertEquals(dateTime, created.getDateTime());
        assertEquals("Super Tour", created.getComment());
        assertEquals(4, created.getDifficulty());
        assertEquals(12.5, created.getTotalDistance());
        assertEquals(90, created.getTotalTime());
        assertEquals(5, created.getRating());
        assertEquals(1L, created.getTour().getId());
    }

    @Test
    void createThrowsForbiddenForForeignTour() {
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour(1L, 1L)));

        assertThrows(ForbiddenException.class, () -> tourLogService.create(1L, 99L,
                new TourLogRequestParams(LocalDateTime.now(), null, 4, 12.5, 90, 5)));
    }

    @Test
    void deleteThrowsForbiddenIfLogBelongsToOtherTour() {
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour(1L, 1L)));
        TourLog log = new TourLog();
        log.setId(7L);
        log.setTour(tour(2L, 1L));
        when(tourLogRepository.findById(7L)).thenReturn(Optional.of(log));

        assertThrows(ForbiddenException.class, () -> tourLogService.delete(1L, 7L, 1L));
    }

    @Test
    void deleteRemovesOwnLog() {
        Tour tour = tour(1L, 1L);
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        TourLog log = new TourLog();
        log.setId(7L);
        log.setTour(tour);
        when(tourLogRepository.findById(7L)).thenReturn(Optional.of(log));

        tourLogService.delete(1L, 7L, 1L);

        verify(tourLogRepository).delete(log);
    }
}
