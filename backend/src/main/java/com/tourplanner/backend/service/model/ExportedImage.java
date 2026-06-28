package com.tourplanner.backend.service.model;

import jakarta.validation.constraints.NotBlank;

public record ExportedImage(
        @NotBlank String filename,
        @NotBlank String base64
) {}
