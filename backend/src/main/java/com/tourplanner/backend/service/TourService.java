package com.tourplanner.backend.service;

import com.tourplanner.backend.model.Tour;
import com.tourplanner.backend.model.TransportType;
import com.tourplanner.backend.model.User;
import com.tourplanner.backend.data.TourRepository;
import com.tourplanner.backend.data.UserRepository;
import com.tourplanner.backend.service.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.core.io.Resource;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TourService {

    private final TourRepository tourRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;

    @Transactional
    public Tour create(Long userId, TourRequestParams params) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User nicht gefunden: " + userId));
        TransportType type = parseTransportType(params.transportType());

        Tour tour = new Tour();
        tour.setUser(user);
        tour.setName(params.name());
        tour.setDescription(params.description());
        tour.setFromLocation(params.fromLocation());
        tour.setToLocation(params.toLocation());
        tour.setTransportType(type);
        tour.setTourDistance(params.tourDistance());
        tour.setEstimatedTime(params.estimatedTime());

        Tour saved = tourRepository.save(tour);
        log.info("Tour created: id={}, name={}, userId={}", saved.getId(), saved.getName(), userId);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Tour> getAllByUser(Long userId) {
        return tourRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public Tour getById(Long id, Long userId) {
        return findTourByUser(id, userId);
    }

    @Transactional
    public Tour update(Long id, Long userId, TourRequestParams params) {
        Tour tour = findTourByUser(id, userId);
        TransportType type = parseTransportType(params.transportType());

        tour.setName(params.name());
        tour.setDescription(params.description());
        tour.setFromLocation(params.fromLocation());
        tour.setToLocation(params.toLocation());
        tour.setTransportType(type);
        tour.setTourDistance(params.tourDistance());
        tour.setEstimatedTime(params.estimatedTime());

        Tour saved = tourRepository.save(tour);
        log.info("Tour updated: id={}, userId={}", saved.getId(), userId);
        return saved;
    }

    @Transactional
    public Tour uploadImage(Long id, Long userId, byte[] imageData, String originalFilename) {
        Tour tour = findTourByUser(id, userId);
        imageService.delete(tour.getTourImagePath());
        String filename = imageService.save(id, imageData, originalFilename);
        tour.setTourImagePath(filename);
        Tour saved = tourRepository.save(tour);
        log.info("Tour image uploaded: id={}, file={}", id, filename);
        return saved;
    }

    @Transactional(readOnly = true)
    public Resource loadImage(Long id, Long userId) {
        Tour tour = findTourByUser(id, userId);
        if (tour.getTourImagePath() == null) {
            return null;
        }
        return imageService.load(tour.getTourImagePath());
    }

    public String getImagePath(Long id, Long userId) {
        return findTourByUser(id, userId).getTourImagePath();
    }

    @Transactional
    public Tour deleteImage(Long id, Long userId) {
        Tour tour = findTourByUser(id, userId);
        if (tour.getTourImagePath() == null) {
            throw new ResourceNotFoundException("Kein Bild vorhanden für Tour: " + id);
        }
        imageService.delete(tour.getTourImagePath());
        tour.setTourImagePath(null);
        Tour saved = tourRepository.save(tour);
        log.info("Tour image deleted: id={}, userId={}", id, userId);
        return saved;
    }

    @Transactional
    public void delete(Long id, Long userId) {
        Tour tour = findTourByUser(id, userId);
        imageService.delete(tour.getTourImagePath());
        tourRepository.delete(tour);
        log.info("Tour deleted: id={}, userId={}", id, userId);
    }

    private Tour findTourByUser(Long id, Long userId) {
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour nicht gefunden: " + id));
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