package com.tourplanner.backend.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDTO {

    @NotBlank
    private String usernameOrEmail;

    @NotBlank
    private String password;
}