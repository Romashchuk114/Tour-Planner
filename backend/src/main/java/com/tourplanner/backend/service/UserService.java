package com.tourplanner.backend.service;

import com.tourplanner.backend.business.User;
import com.tourplanner.backend.data.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public Map<String, Object> register(String username, String email, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Benutzername bereits vergeben");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("E-Mail bereits vergeben");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        User saved = userRepository.save(user);

        String token = jwtService.generateToken(saved);
        log.info("User registered: id={}, username={}", saved.getId(), saved.getUsername());

        return Map.of(
                "id", saved.getId(),
                "username", saved.getUsername(),
                "email", saved.getEmail(),
                "token", token
        );
    }

    public Map<String, Object> login(String usernameOrEmail, String password) {
        User user = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> new IllegalArgumentException("Benutzer nicht gefunden"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Falsches Passwort");
        }

        String token = jwtService.generateToken(user);
        log.info("User logged in: id={}, username={}", user.getId(), user.getUsername());

        return Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "token", token
        );
    }
}