package com.tourplanner.backend.service;

public record TourRequestParams(
        String name,
        String description,
        String fromLocation,
        String toLocation,
        String transportType,
        Double tourDistance,
        Integer estimatedTime
) {}