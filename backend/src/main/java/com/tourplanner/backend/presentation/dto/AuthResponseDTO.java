package com.tourplanner.backend.presentation.dto;

public record AuthResponseDTO(
        Long id,
        String username,
        String email,
        String token
) {}