package com.tourplanner.backend.service.openrouteservice;

public record RouteInfo(
        Double distanceKm,
        Integer durationMinutes,
        String geometryGeoJson
) {}
