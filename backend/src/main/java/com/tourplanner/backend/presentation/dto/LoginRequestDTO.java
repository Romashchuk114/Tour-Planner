package com.tourplanner.backend.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @NotBlank String usernameOrEmail,
        @NotBlank String password
) {}