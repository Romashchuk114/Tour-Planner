package com.tourplanner.backend.service;

import com.tourplanner.backend.business.User;
import com.tourplanner.backend.data.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User register(String username, String email, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Benutzername bereits vergeben");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("E-Mail bereits vergeben");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password); // TODO: später hashen
        return userRepository.save(user);
    }

    public User login(String usernameOrEmail, String password) {
        User user = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> new IllegalArgumentException("Benutzer nicht gefunden"));

        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("Falsches Passwort");
        }

        return user;
    }
}