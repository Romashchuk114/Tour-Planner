package com.tourplanner.backend.presentation.controller;

import com.tourplanner.backend.presentation.dto.LoginRequestDTO;
import com.tourplanner.backend.presentation.dto.RegisterRequestDTO;
import com.tourplanner.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDTO request) {
        try {
            return ResponseEntity.ok(userService.register(
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO request) {
        try {
            return ResponseEntity.ok(userService.login(
                    request.getUsernameOrEmail(),
                    request.getPassword()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}