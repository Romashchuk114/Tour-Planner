package com.tourplanner.backend.presentation.controller;

import com.tourplanner.backend.config.AuthenticatedUser;
import com.tourplanner.backend.model.Tour;
import com.tourplanner.backend.model.TourStage;
import com.tourplanner.backend.presentation.dto.StageResponseDTO;
import com.tourplanner.backend.presentation.dto.TourRequestDTO;
import com.tourplanner.backend.presentation.dto.TourResponseDTO;
import com.tourplanner.backend.service.ComputedAttributes;
import com.tourplanner.backend.service.StageParam;
import com.tourplanner.backend.service.TourAttributeService;
import com.tourplanner.backend.service.TourRequestParams;
import com.tourplanner.backend.service.TourService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tours")
@RequiredArgsConstructor
public class TourController {

    private final TourService tourService;
    private final TourAttributeService tourAttributeService;

    @GetMapping
    public ResponseEntity<List<TourResponseDTO>> getAll(@AuthenticationPrincipal AuthenticatedUser user,
                                                         @RequestParam(name = "q", required = false) String query) {
        Map<Long, ComputedAttributes> attributes = tourAttributeService.computeForUser(user.id());
        List<Tour> tours = tourService.search(user.id(), query, attributes);
        return ResponseEntity.ok(tours.stream()
                .map(tour -> toResponse(tour, attributes.getOrDefault(tour.getId(), ComputedAttributes.NONE)))
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TourResponseDTO> getById(@PathVariable Long id,
                                                    @AuthenticationPrincipal AuthenticatedUser user) {
        Tour tour = tourService.getById(id, user.id());
        return ResponseEntity.ok(toResponse(tour));
    }

    @PostMapping
    public ResponseEntity<TourResponseDTO> create(@AuthenticationPrincipal AuthenticatedUser user,
                                                   @Valid @RequestBody TourRequestDTO request) {
        Tour tour = tourService.create(user.id(), toParams(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(tour));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TourResponseDTO> update(@PathVariable Long id,
                                                   @AuthenticationPrincipal AuthenticatedUser user,
                                                   @Valid @RequestBody TourRequestDTO request) {
        Tour tour = tourService.update(id, user.id(), toParams(request));
        return ResponseEntity.ok(toResponse(tour));
    }

    @PostMapping("/{id}/image")
    public ResponseEntity<TourResponseDTO> uploadImage(@PathVariable Long id,
                                                        @AuthenticationPrincipal AuthenticatedUser user,
                                                        @RequestParam("file") MultipartFile file) throws IOException {
        Tour tour = tourService.uploadImage(id, user.id(), file.getBytes(), file.getOriginalFilename());
        return ResponseEntity.ok(toResponse(tour));
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<Resource> getImage(@PathVariable Long id,
                                              @AuthenticationPrincipal AuthenticatedUser user) {
        String imagePath = tourService.getImagePath(id, user.id());
        if (imagePath == null) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = tourService.loadImage(id, user.id());
        return ResponseEntity.ok()
                .contentType(MediaTypeFactory.getMediaType(imagePath).orElse(MediaType.APPLICATION_OCTET_STREAM))
                .body(resource);
    }
    
    @GetMapping("/{id}/report")
    public ResponseEntity<byte[]> getTourReport(@PathVariable Long id,
                                                 @AuthenticationPrincipal AuthenticatedUser user) {
        byte[] pdfContents = tourService.generateTourReport(id, user.id());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String filename = "tour_report_" + id + ".pdf";
        headers.setContentDispositionFormData(filename, filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        return new ResponseEntity<>(pdfContents, headers, HttpStatus.OK);
    }

    @DeleteMapping("/{id}/image")
    public ResponseEntity<TourResponseDTO> deleteImage(@PathVariable Long id,
                                                        @AuthenticationPrincipal AuthenticatedUser user) {
        Tour tour = tourService.deleteImage(id, user.id());
        return ResponseEntity.ok(toResponse(tour));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                        @AuthenticationPrincipal AuthenticatedUser user) {
        tourService.delete(id, user.id());
        return ResponseEntity.noContent().build();
    }

    private TourRequestParams toParams(TourRequestDTO dto) {
        List<StageParam> stages = dto.stages().stream()
                .map(s -> new StageParam(
                        s.transportType(), s.endName(), s.endLat(), s.endLng()))
                .toList();
        return new TourRequestParams(
                dto.name(), dto.description(),
                dto.fromName(), dto.fromLat(), dto.fromLng(),
                stages
        );
    }

    private TourResponseDTO toResponse(Tour tour) {
        return toResponse(tour, tourAttributeService.computeForTour(tour.getId()));
    }

    private TourResponseDTO toResponse(Tour tour, ComputedAttributes attributes) {
        List<StageResponseDTO> stages = tour.getStages().stream()
                .map(s -> new StageResponseDTO(
                        s.getOrderIndex(),
                        s.getTransportType().name(),
                        s.getEndName(),
                        s.getEndLat(),
                        s.getEndLng(),
                        s.getDistance(),
                        s.getDuration(),
                        s.getGeometryGeoJson()))
                .toList();

        double totalDistance = tour.getStages().stream().mapToDouble(TourStage::getDistance).sum();
        int totalDuration = tour.getStages().stream().mapToInt(TourStage::getDuration).sum();

        return new TourResponseDTO(
                tour.getId(),
                tour.getUser().getId(),
                tour.getName(),
                tour.getDescription(),
                tour.getFromName(),
                tour.getFromLat(),
                tour.getFromLng(),
                Math.round(totalDistance * 100.0) / 100.0,
                totalDuration,
                attributes.logCount(),
                attributes.popularity(),
                attributes.childFriendliness(),
                tour.getTourImagePath(),
                stages,
                tour.getCreatedAt(),
                tour.getUpdatedAt()
        );
    }
}
