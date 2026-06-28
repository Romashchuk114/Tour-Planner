package com.tourplanner.backend.service.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record TourExportEnvelope(
        @NotNull Integer formatVersion,
        LocalDateTime exportedAt,
        @NotEmpty @Valid List<ExportedTour> tours
) {}
