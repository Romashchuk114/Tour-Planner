package com.tourplanner.backend.service;

import com.tourplanner.backend.model.TransportType;
import com.tourplanner.backend.service.openrouteservice.OpenRouteServiceClient;
import com.tourplanner.backend.service.openrouteservice.RouteInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteService {

    private final OpenRouteServiceClient client;

    public RouteInfo fetchRoute(double fromLat, double fromLng, double toLat, double toLng, TransportType type) {
        String profile = mapToOrsProfile(type);
        RouteInfo info = client.directions(profile, fromLat, fromLng, toLat, toLng);
        log.info("Route geladen: [{},{}] → [{},{}] via {} = {} km, {} min",
                fromLat, fromLng, toLat, toLng, profile, info.distanceKm(), info.durationMinutes());
        return info;
    }

    private String mapToOrsProfile(TransportType type) {
        return switch (type) {
            case WALK -> "foot-walking";
            case BIKE -> "cycling-regular";
            case CAR -> "driving-car";
            case PUBLIC_TRANSPORT -> {
                log.warn("PUBLIC_TRANSPORT wird auf driving-car gemappt (ORS unterstützt keinen ÖPNV)");
                yield "driving-car";
            }
        };
    }
}
