package com.tourplanner.backend.service;

import com.tourplanner.backend.data.TourRepository;
import com.tourplanner.backend.data.UserRepository;
import com.tourplanner.backend.model.Tour;
import com.tourplanner.backend.model.TourLog;
import com.tourplanner.backend.model.TourStage;
import com.tourplanner.backend.model.User;
import com.tourplanner.backend.service.exception.ForbiddenException;
import com.tourplanner.backend.service.model.ExportedImage;
import com.tourplanner.backend.service.model.ExportedLog;
import com.tourplanner.backend.service.model.ExportedStage;
import com.tourplanner.backend.service.model.ExportedTour;
import com.tourplanner.backend.service.model.TourExportEnvelope;
import com.tourplanner.backend.service.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TourImportExportService {

    private static final int FORMAT_VERSION = 1;

    private final TourRepository tourRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;

    @Transactional(readOnly = true)
    public TourExportEnvelope exportAll(Long userId) {
        List<Tour> tours = tourRepository.findByUserIdOrderByCreatedAtDesc(userId);
        log.info("Export: {} Touren exportiert (userId={})", tours.size(), userId);
        return toEnvelope(tours);
    }

    @Transactional(readOnly = true)
    public TourExportEnvelope exportOne(Long tourId, Long userId) {
        Tour tour = findTourByUser(tourId, userId);
        log.info("Export: Tour {} exportiert (userId={})", tourId, userId);
        return toEnvelope(List.of(tour));
    }

    @Transactional
    public List<Tour> importTours(Long userId, TourExportEnvelope envelope) {
        if (envelope.formatVersion() != FORMAT_VERSION) {
            throw new IllegalArgumentException("Nicht unterstützte Format-Version: " + envelope.formatVersion());
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User nicht gefunden: " + userId));

        List<Tour> imported = new ArrayList<>();
        for (ExportedTour exportedTour : envelope.tours()) {
            imported.add(importTour(user, exportedTour));
        }
        log.info("Import: {} Touren importiert (userId={})", imported.size(), userId);
        return imported;
    }

    private Tour importTour(User user, ExportedTour exportedTour) {
        Tour tour = new Tour();
        tour.setUser(user);
        tour.setName(exportedTour.name());
        tour.setDescription(exportedTour.description());
        tour.setFromName(exportedTour.fromName());
        tour.setFromLat(exportedTour.fromLat());
        tour.setFromLng(exportedTour.fromLng());

        for (int i = 0; i < exportedTour.stages().size(); i++) {
            ExportedStage exportedStage = exportedTour.stages().get(i);
            TourStage stage = new TourStage();
            stage.setTour(tour);
            stage.setOrderIndex(i);
            stage.setTransportType(exportedStage.transportType());
            stage.setEndName(exportedStage.endName());
            stage.setEndLat(exportedStage.endLat());
            stage.setEndLng(exportedStage.endLng());
            stage.setDistance(exportedStage.distance());
            stage.setDuration(exportedStage.duration());
            stage.setGeometryGeoJson(exportedStage.geometryGeoJson());
            tour.getStages().add(stage);
        }

        for (ExportedLog exportedLog : exportedTour.logs() == null ? List.<ExportedLog>of() : exportedTour.logs()) {
            TourLog tourLog = new TourLog();
            tourLog.setTour(tour);
            tourLog.setDateTime(exportedLog.dateTime());
            tourLog.setComment(exportedLog.comment());
            tourLog.setDifficulty(exportedLog.difficulty());
            tourLog.setTotalDistance(exportedLog.totalDistance());
            tourLog.setTotalTime(exportedLog.totalTime());
            tourLog.setRating(exportedLog.rating());
            tour.getLogs().add(tourLog);
        }

        Tour saved = tourRepository.saveAndFlush(tour);

        if (exportedTour.image() != null) {
            byte[] data = decodeBase64(exportedTour.image().base64());
            String filename = imageService.save(saved.getId(), data, exportedTour.image().filename());
            saved.setTourImagePath(filename);
        }
        return saved;
    }

    private TourExportEnvelope toEnvelope(List<Tour> tours) {
        List<ExportedTour> exported = tours.stream().map(this::toExportedTour).toList();
        return new TourExportEnvelope(FORMAT_VERSION, LocalDateTime.now(), exported);
    }

    private ExportedTour toExportedTour(Tour tour) {
        List<ExportedStage> stages = tour.getStages().stream()
                .map(s -> new ExportedStage(
                        s.getOrderIndex(),
                        s.getTransportType(),
                        s.getEndName(),
                        s.getEndLat(),
                        s.getEndLng(),
                        s.getDistance(),
                        s.getDuration(),
                        s.getGeometryGeoJson()))
                .toList();

        List<ExportedLog> logs = tour.getLogs().stream()
                .map(l -> new ExportedLog(
                        l.getDateTime(),
                        l.getComment(),
                        l.getDifficulty(),
                        l.getTotalDistance(),
                        l.getTotalTime(),
                        l.getRating()))
                .toList();

        return new ExportedTour(
                tour.getName(),
                tour.getDescription(),
                tour.getFromName(),
                tour.getFromLat(),
                tour.getFromLng(),
                stages,
                logs,
                exportImage(tour)
        );
    }

    private ExportedImage exportImage(Tour tour) {
        if (tour.getTourImagePath() == null) {
            return null;
        }
        try {
            byte[] data = imageService.load(tour.getTourImagePath()).getContentAsByteArray();
            return new ExportedImage(tour.getTourImagePath(), Base64.getEncoder().encodeToString(data));
        } catch (ResourceNotFoundException | IOException e) {
            log.warn("Bild für Export nicht lesbar, wird übersprungen: {}", tour.getTourImagePath());
            return null;
        }
    }

    private byte[] decodeBase64(String base64) {
        try {
            return Base64.getDecoder().decode(base64);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Bilddaten sind kein gültiges Base64");
        }
    }

    private Tour findTourByUser(Long id, Long userId) {
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour nicht gefunden: " + id));
        if (!tour.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Kein Zugriff auf diese Tour");
        }
        return tour;
    }
}
