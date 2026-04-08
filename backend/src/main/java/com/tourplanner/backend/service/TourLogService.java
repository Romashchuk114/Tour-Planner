package com.tourplanner.backend.service;

import com.tourplanner.backend.model.Tour;
import com.tourplanner.backend.model.TourLog;
import com.tourplanner.backend.data.TourLogRepository;
import com.tourplanner.backend.data.TourRepository;
import com.tourplanner.backend.service.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TourLogService {

    private final TourLogRepository tourLogRepository;
    private final TourRepository tourRepository;

    @Transactional
    public TourLog create(Long tourId, Long userId, TourLogRequestParams params) {
        Tour tour = findTourByUser(tourId, userId);

        TourLog tourLog = new TourLog();
        tourLog.setTour(tour);
        mapFields(tourLog, params);

        TourLog saved = tourLogRepository.save(tourLog);
        log.info("TourLog created: id={}, tourId={}", saved.getId(), tourId);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<TourLog> getByTourId(Long tourId, Long userId) {
        findTourByUser(tourId, userId);
        return tourLogRepository.findByTourIdOrderByDateTimeDesc(tourId);
    }

    @Transactional
    public TourLog update(Long tourId, Long logId, Long userId, TourLogRequestParams params) {
        findTourByUser(tourId, userId);
        TourLog tourLog = findLog(logId, tourId);
        mapFields(tourLog, params);

        TourLog saved = tourLogRepository.save(tourLog);
        log.info("TourLog updated: id={}, tourId={}", saved.getId(), tourId);
        return saved;
    }

    @Transactional
    public void delete(Long tourId, Long logId, Long userId) {
        findTourByUser(tourId, userId);
        TourLog tourLog = findLog(logId, tourId);
        tourLogRepository.delete(tourLog);
        log.info("TourLog deleted: id={}, tourId={}", logId, tourId);
    }

    private void mapFields(TourLog tourLog, TourLogRequestParams params) {
        tourLog.setDateTime(params.dateTime());
        tourLog.setComment(params.comment());
        tourLog.setDifficulty(params.difficulty());
        tourLog.setTotalDistance(params.totalDistance());
        tourLog.setTotalTime(params.totalTime());
        tourLog.setRating(params.rating());
    }

    private Tour findTourByUser(Long tourId, Long userId) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour nicht gefunden: " + tourId));
        if (!tour.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Kein Zugriff auf diese Tour");
        }
        return tour;
    }

    private TourLog findLog(Long logId, Long tourId) {
        TourLog tourLog = tourLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("Log nicht gefunden: " + logId));
        if (!tourLog.getTour().getId().equals(tourId)) {
            throw new IllegalArgumentException("Log gehört nicht zu Tour " + tourId);
        }
        return tourLog;
    }
}