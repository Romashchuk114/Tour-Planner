package com.tourplanner.backend.presentation.dto;

import java.time.LocalDateTime;

public record ErrorResponseDTO(
        int status,
        String error,
        LocalDateTime timestamp
) {}
