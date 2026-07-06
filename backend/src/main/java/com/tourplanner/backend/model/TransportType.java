package com.tourplanner.backend.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransportType {
    WALK("Zu Fuß"),
    HIKING("Wandern"),
    BIKE("Fahrrad"),
    MOUNTAIN_BIKE("Mountainbike"),
    ROAD_BIKE("Rennrad"),
    CAR("Auto"),
    MOTORHOME("Wohnmobil / LKW");

    private final String label;
}
