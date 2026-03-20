package com.tourplanner.backend.service;

import com.tourplanner.backend.business.Tour;
import com.tourplanner.backend.business.TransportType;
import com.tourplanner.backend.business.User;
import com.tourplanner.backend.data.TourRepository;
import com.tourplanner.backend.presentation.dto.TourRequestDTO;
import com.tourplanner.backend.presentation.dto.TourResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TourService {

    private final TourRepository tourRepository;

    public TourResponseDTO create(User user, TourRequestDTO dto) {
        TransportType type = parseTransportType(dto.getTransportType());

        Tour tour = new Tour();
        tour.setUser(user);
        tour.setName(dto.getName());
        tour.setDescription(dto.getDescription());
        tour.setFromLocation(dto.getFromLocation());
        tour.setToLocation(dto.getToLocation());
        tour.setTransportType(type);
        tour.setTourDistance(dto.getTourDistance());
        tour.setEstimatedTime(dto.getEstimatedTime());

        Tour saved = tourRepository.save(tour);
        log.info("Tour created: id={}, name={}, userId={}", saved.getId(), saved.getName(), user.getId());
        return new TourResponseDTO(saved, 0);
    }

    public List<TourResponseDTO> getAllByUser(Long userId) {
        return tourRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(tour -> new TourResponseDTO(tour, tour.getLogs().size()))
                .toList();
    }

    public TourResponseDTO getById(Long id, Long userId) {
        Tour tour = findTourByUser(id, userId);
        return new TourResponseDTO(tour, tour.getLogs().size());
    }

    public TourResponseDTO update(Long id, Long userId, TourRequestDTO dto) {
        Tour tour = findTourByUser(id, userId);
        TransportType type = parseTransportType(dto.getTransportType());

        tour.setName(dto.getName());
        tour.setDescription(dto.getDescription());
        tour.setFromLocation(dto.getFromLocation());
        tour.setToLocation(dto.getToLocation());
        tour.setTransportType(type);
        tour.setTourDistance(dto.getTourDistance());
        tour.setEstimatedTime(dto.getEstimatedTime());

        Tour saved = tourRepository.save(tour);
        log.info("Tour updated: id={}, userId={}", saved.getId(), userId);
        return new TourResponseDTO(saved, saved.getLogs().size());
    }

    public void delete(Long id, Long userId) {
        Tour tour = findTourByUser(id, userId);
        tourRepository.delete(tour);
        log.info("Tour deleted: id={}, userId={}", id, userId);
    }

    private Tour findTourByUser(Long id, Long userId) {
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tour nicht gefunden: " + id));
        if (!tour.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Kein Zugriff auf diese Tour");
        }
        return tour;
    }

    private TransportType parseTransportType(String value) {
        try {
            return TransportType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Ungültiger Transporttyp: " + value);
        }
    }
}
