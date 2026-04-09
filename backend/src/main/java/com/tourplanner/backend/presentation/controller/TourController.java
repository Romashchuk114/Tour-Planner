package com.tourplanner.backend.presentation.controller;

import com.tourplanner.backend.config.AuthenticatedUser;
import com.tourplanner.backend.model.Tour;
import com.tourplanner.backend.presentation.dto.TourRequestDTO;
import com.tourplanner.backend.presentation.dto.TourResponseDTO;
import com.tourplanner.backend.service.TourRequestParams;
import com.tourplanner.backend.service.TourService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/tours")
@RequiredArgsConstructor
public class TourController {

    private final TourService tourService;

    @GetMapping
    public ResponseEntity<List<TourResponseDTO>> getAll(@AuthenticationPrincipal AuthenticatedUser user) {
        List<Tour> tours = tourService.getAllByUser(user.id());
        return ResponseEntity.ok(tours.stream().map(this::toResponse).toList());
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                        @AuthenticationPrincipal AuthenticatedUser user) {
        tourService.delete(id, user.id());
        return ResponseEntity.noContent().build();
    }

    private TourRequestParams toParams(TourRequestDTO dto) {
        return new TourRequestParams(
                dto.name(), dto.description(), dto.fromLocation(), dto.toLocation(),
                dto.transportType(), dto.tourDistance(), dto.estimatedTime()
        );
    }

    private TourResponseDTO toResponse(Tour tour) {
        return new TourResponseDTO(
                tour.getId(),
                tour.getUser().getId(),
                tour.getName(),
                tour.getDescription(),
                tour.getFromLocation(),
                tour.getToLocation(),
                tour.getTransportType().name(),
                tour.getTourDistance(),
                tour.getEstimatedTime(),
                tour.getTourImagePath(),
                tour.getCreatedAt(),
                tour.getUpdatedAt()
        );
    }
}