package com.tourplanner.backend.service;

import com.tourplanner.backend.model.Tour;
import com.tourplanner.backend.model.TourStage;
import com.tourplanner.backend.model.User;
import com.tourplanner.backend.data.TourRepository;
import com.tourplanner.backend.data.UserRepository;
import com.tourplanner.backend.service.exception.ForbiddenException;
import com.tourplanner.backend.service.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TourService {

    private final TourRepository tourRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;
    private final RouteService routeService;
    private final PdfService pdfService;

    @Transactional
    public Tour create(Long userId, TourRequestParams params) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User nicht gefunden: " + userId));

        Tour tour = new Tour();
        tour.setUser(user);
        applyHeader(tour, params);
        rebuildStages(tour, params);

        Tour saved = tourRepository.save(tour);
        log.info("Tour created: id={}, name={}, userId={}, stages={}",
                saved.getId(), saved.getName(), userId, saved.getStages().size());
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
        applyHeader(tour, params);
        // Flush DELETE before re-INSERT — sonst kollidiert (tour_id, order_index) UNIQUE im selben Flush
        tour.getStages().clear();
        tourRepository.saveAndFlush(tour);
        rebuildStages(tour, params);

        Tour saved = tourRepository.save(tour);
        log.info("Tour updated: id={}, userId={}, stages={}", saved.getId(), userId, saved.getStages().size());
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

    @Transactional(readOnly = true)
    public String getImagePath(Long id, Long userId) {
        return findTourByUser(id, userId).getTourImagePath();
    }
    
    @Transactional(readOnly = true)
    public byte[] generateTourReport(Long tourId, Long userId) {
        Tour tour = findTourByUser(tourId, userId);
        return pdfService.createTourReport(tour);
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
            throw new ForbiddenException("Kein Zugriff auf diese Tour");
        }
        return tour;
    }

    private void applyHeader(Tour tour, TourRequestParams params) {
        tour.setName(params.name());
        tour.setDescription(params.description());
        tour.setFromName(params.fromName());
        tour.setFromLat(params.fromLat());
        tour.setFromLng(params.fromLng());
    }

    private void rebuildStages(Tour tour, TourRequestParams params) {
        double prevLat = params.fromLat();
        double prevLng = params.fromLng();
        for (int i = 0; i < params.stages().size(); i++) {
            StageParam p = params.stages().get(i);
            RouteInfo info = routeService.fetchRoute(prevLat, prevLng, p.endLat(), p.endLng(), p.transportType());

            TourStage stage = new TourStage();
            stage.setTour(tour);
            stage.setOrderIndex(i);
            stage.setTransportType(p.transportType());
            stage.setEndName(p.endName());
            stage.setEndLat(p.endLat());
            stage.setEndLng(p.endLng());
            stage.setDistance(info.distanceKm());
            stage.setDuration(info.durationMinutes());
            stage.setGeometryGeoJson(info.geometryGeoJson());
            tour.getStages().add(stage);

            prevLat = p.endLat();
            prevLng = p.endLng();
        }
    }
}
