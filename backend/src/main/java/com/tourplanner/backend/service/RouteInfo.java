package com.tourplanner.backend.service;

public record RouteInfo(
        Double distanceKm,
        Integer durationMinutes,
        String geometryGeoJson
) {}
