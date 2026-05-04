package com.tourplanner.backend.service;

import com.tourplanner.backend.model.TransportType;

public record StageParam(
        TransportType transportType,
        String endName,
        Double endLat,
        Double endLng
) {}
