package com.tourplanner.backend.presentation.controller;

import com.tourplanner.backend.config.AuthenticatedUser;
import com.tourplanner.backend.model.TourLog;
import com.tourplanner.backend.presentation.dto.TourLogRequestDTO;
import com.tourplanner.backend.presentation.dto.TourLogResponseDTO;
import com.tourplanner.backend.service.TourLogRequestParams;
import com.tourplanner.backend.service.TourLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tours/{tourId}/logs")
@RequiredArgsConstructor
public class TourLogController {

    private final TourLogService tourLogService;

    @GetMapping
    public ResponseEntity<List<TourLogResponseDTO>> getByTourId(@PathVariable Long tourId,
                                                                 @AuthenticationPrincipal AuthenticatedUser user) {
        List<TourLog> logs = tourLogService.getByTourId(tourId, user.id());
        return ResponseEntity.ok(logs.stream().map(this::toResponse).toList());
    }

    @PostMapping
    public ResponseEntity<TourLogResponseDTO> create(@PathVariable Long tourId,
                                                      @AuthenticationPrincipal AuthenticatedUser user,
                                                      @Valid @RequestBody TourLogRequestDTO request) {
        TourLog log = tourLogService.create(tourId, user.id(), toParams(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(log));
    }

    @PutMapping("/{logId}")
    public ResponseEntity<TourLogResponseDTO> update(@PathVariable Long tourId,
                                                      @PathVariable Long logId,
                                                      @AuthenticationPrincipal AuthenticatedUser user,
                                                      @Valid @RequestBody TourLogRequestDTO request) {
        TourLog log = tourLogService.update(tourId, logId, user.id(), toParams(request));
        return ResponseEntity.ok(toResponse(log));
    }

    @DeleteMapping("/{logId}")
    public ResponseEntity<Void> delete(@PathVariable Long tourId,
                                        @PathVariable Long logId,
                                        @AuthenticationPrincipal AuthenticatedUser user) {
        tourLogService.delete(tourId, logId, user.id());
        return ResponseEntity.noContent().build();
    }

    private TourLogRequestParams toParams(TourLogRequestDTO dto) {
        return new TourLogRequestParams(
                dto.dateTime(), dto.comment(), dto.difficulty(),
                dto.totalDistance(), dto.totalTime(), dto.rating()
        );
    }

    private TourLogResponseDTO toResponse(TourLog tourLog) {
        return new TourLogResponseDTO(
                tourLog.getId(),
                tourLog.getTour().getId(),
                tourLog.getDateTime(),
                tourLog.getComment(),
                tourLog.getDifficulty(),
                tourLog.getTotalDistance(),
                tourLog.getTotalTime(),
                tourLog.getRating(),
                tourLog.getCreatedAt(),
                tourLog.getUpdatedAt()
        );
    }
}