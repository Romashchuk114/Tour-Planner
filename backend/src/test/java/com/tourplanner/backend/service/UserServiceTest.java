package com.tourplanner.backend.service;

import com.tourplanner.backend.data.UserRepository;
import com.tourplanner.backend.model.User;
import com.tourplanner.backend.service.exception.InvalidCredentialsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void registerStoresEncodedPasswordOnly() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("a@b.at")).thenReturn(false);
        when(passwordEncoder.encode("geheim123")).thenReturn("$2a$hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User registered = userService.register("testuser", "a@b.at", "geheim123");

        assertEquals("$2a$hash", registered.getPassword());
        assertEquals("testuser", registered.getUsername());
    }

    @Test
    void registerRejectsDuplicateUsername() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> userService.register("testuser", "a@b.at", "geheim123"));
    }

    @Test
    void loginRejectsWrongPassword() {
        User user = new User();
        user.setPassword("$2a$hash");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("falsch", "$2a$hash")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> userService.login("testuser", "falsch"));
    }

    @Test
    void loginFallsBackToEmailLookup() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("$2a$hash");
        when(userRepository.findByUsername("a@b.at")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("a@b.at")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("geheim123", "$2a$hash")).thenReturn(true);

        assertEquals("testuser", userService.login("a@b.at", "geheim123").getUsername());
    }
}
