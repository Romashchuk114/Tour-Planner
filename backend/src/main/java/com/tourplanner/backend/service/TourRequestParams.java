package com.tourplanner.backend.service;

public record TourRequestParams(
        String name,
        String description,
        String fromLocation,
        String toLocation,
        Double fromLat,
        Double fromLng,
        Double toLat,
        Double toLng,
        String transportType
) {}