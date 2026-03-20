package com.tourplanner.backend.service;

import com.tourplanner.backend.business.Tour;
import com.tourplanner.backend.business.TourLog;
import com.tourplanner.backend.data.TourLogRepository;
import com.tourplanner.backend.data.TourRepository;
import com.tourplanner.backend.presentation.dto.TourLogRequestDTO;
import com.tourplanner.backend.presentation.dto.TourLogResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TourLogService {

    private final TourLogRepository tourLogRepository;
    private final TourRepository tourRepository;

    public TourLogResponseDTO create(Long tourId, Long userId, TourLogRequestDTO dto) {
        Tour tour = findTourByUser(tourId, userId);

        TourLog tourLog = new TourLog();
        tourLog.setTour(tour);
        tourLog.setDateTime(dto.getDateTime());
        tourLog.setComment(dto.getComment());
        tourLog.setDifficulty(dto.getDifficulty());
        tourLog.setTotalDistance(dto.getTotalDistance());
        tourLog.setTotalTime(dto.getTotalTime());
        tourLog.setRating(dto.getRating());

        TourLog saved = tourLogRepository.save(tourLog);
        log.info("TourLog created: id={}, tourId={}", saved.getId(), tourId);
        return new TourLogResponseDTO(saved);
    }

    public List<TourLogResponseDTO> getByTourId(Long tourId, Long userId) {
        findTourByUser(tourId, userId);
        return tourLogRepository.findByTourIdOrderByDateTimeDesc(tourId).stream()
                .map(TourLogResponseDTO::new)
                .toList();
    }

    public TourLogResponseDTO update(Long tourId, Long logId, Long userId, TourLogRequestDTO dto) {
        findTourByUser(tourId, userId);
        TourLog tourLog = findLog(logId, tourId);

        tourLog.setDateTime(dto.getDateTime());
        tourLog.setComment(dto.getComment());
        tourLog.setDifficulty(dto.getDifficulty());
        tourLog.setTotalDistance(dto.getTotalDistance());
        tourLog.setTotalTime(dto.getTotalTime());
        tourLog.setRating(dto.getRating());

        TourLog saved = tourLogRepository.save(tourLog);
        log.info("TourLog updated: id={}, tourId={}", saved.getId(), tourId);
        return new TourLogResponseDTO(saved);
    }

    public void delete(Long tourId, Long logId, Long userId) {
        findTourByUser(tourId, userId);
        TourLog tourLog = findLog(logId, tourId);
        tourLogRepository.delete(tourLog);
        log.info("TourLog deleted: id={}, tourId={}", logId, tourId);
    }

    private Tour findTourByUser(Long tourId, Long userId) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new EntityNotFoundException("Tour nicht gefunden: " + tourId));
        if (!tour.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Kein Zugriff auf diese Tour");
        }
        return tour;
    }

    private TourLog findLog(Long logId, Long tourId) {
        TourLog tourLog = tourLogRepository.findById(logId)
                .orElseThrow(() -> new EntityNotFoundException("Log nicht gefunden: " + logId));
        if (!tourLog.getTour().getId().equals(tourId)) {
            throw new IllegalArgumentException("Log gehört nicht zu Tour " + tourId);
        }
        return tourLog;
    }
}