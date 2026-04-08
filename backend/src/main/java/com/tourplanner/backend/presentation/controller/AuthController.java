package com.tourplanner.backend.presentation.controller;

import com.tourplanner.backend.model.User;
import com.tourplanner.backend.presentation.dto.AuthResponseDTO;
import com.tourplanner.backend.presentation.dto.LoginRequestDTO;
import com.tourplanner.backend.presentation.dto.RegisterRequestDTO;
import com.tourplanner.backend.service.JwtService;
import com.tourplanner.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        User user = userService.register(request.username(), request.email(), request.password());
        return ResponseEntity.ok(toResponse(user));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        User user = userService.login(request.usernameOrEmail(), request.password());
        return ResponseEntity.ok(toResponse(user));
    }

    private AuthResponseDTO toResponse(User user) {
        String token = jwtService.generateToken(user);
        return new AuthResponseDTO(user.getId(), user.getUsername(), user.getEmail(), token);
    }
}