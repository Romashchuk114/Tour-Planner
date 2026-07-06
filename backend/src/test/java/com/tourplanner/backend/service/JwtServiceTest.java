package com.tourplanner.backend.service;

import com.tourplanner.backend.config.AuthenticatedUser;
import com.tourplanner.backend.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET = "test-secret-mit-mindestens-32-zeichen-laenge!";

    private JwtService jwtService;

    private User user() {
        User user = new User();
        user.setId(7L);
        user.setUsername("testuser");
        return user;
    }

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationMs", 3_600_000L);
    }

    @Test
    void generatedTokenRoundtripsToSameUser() {
        String token = jwtService.generateToken(user());

        Optional<AuthenticatedUser> extracted = jwtService.extractUser(token);

        assertTrue(extracted.isPresent());
        assertEquals(7L, extracted.get().id());
        assertEquals("testuser", extracted.get().username());
    }

    @Test
    void garbageTokenYieldsEmptyOptional() {
        assertTrue(jwtService.extractUser("kein.echtes.token").isEmpty());
    }

    @Test
    void expiredTokenYieldsEmptyOptional() {
        ReflectionTestUtils.setField(jwtService, "expirationMs", -1_000L);
        String expiredToken = jwtService.generateToken(user());

        assertTrue(jwtService.extractUser(expiredToken).isEmpty());
    }
}
