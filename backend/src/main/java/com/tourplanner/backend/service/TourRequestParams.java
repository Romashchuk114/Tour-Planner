package com.tourplanner.backend.service;

import java.util.List;

public record TourRequestParams(
        String name,
        String description,
        String fromName,
        Double fromLat,
        Double fromLng,
        List<StageParam> stages
) {}