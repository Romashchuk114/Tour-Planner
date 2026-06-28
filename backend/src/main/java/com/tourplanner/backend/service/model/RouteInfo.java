package com.tourplanner.backend.service.model;

public record RouteInfo(
        Double distanceKm,
        Integer durationMinutes,
        String geometryGeoJson
) {}
