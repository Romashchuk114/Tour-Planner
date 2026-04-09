package com.tourplanner.backend.service;

import java.time.LocalDateTime;

public record TourLogRequestParams(
        LocalDateTime dateTime,
        String comment,
        Integer difficulty,
        Double totalDistance,
        Integer totalTime,
        Integer rating
) {}