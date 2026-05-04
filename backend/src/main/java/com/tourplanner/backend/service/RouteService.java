package com.tourplanner.backend.service;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.tourplanner.backend.model.TransportType;
import com.tourplanner.backend.service.exception.RouteServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RouteService {

    private static final String ORS_HINT_DISTANCE_TOO_LONG = "approximated route distance";
    private static final String ORS_HINT_NOT_ROUTABLE = "Could not find routable point";

    private final RestClient orsClient;
    private final String apiKey;
    private final ObjectMapper objectMapper;

    public RouteService(@Value("${app.openrouteservice.base-url}") String baseUrl,
                        @Value("${app.openrouteservice.api-key}") String apiKey,
                        ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.objectMapper = objectMapper;
        this.orsClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public RouteInfo fetchRoute(double fromLat, double fromLng, double toLat, double toLng, TransportType type) {
        String profile = mapToOrsProfile(type);
        Map<String, Object> body = Map.of(
                "coordinates", List.of(
                        List.of(fromLng, fromLat),
                        List.of(toLng, toLat)
                )
        );

        try {
            DirectionsResponse response = orsClient.post()
                    .uri("/v2/directions/{profile}/geojson", profile)
                    .header("Authorization", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(DirectionsResponse.class);

            if (response == null || response.features() == null || response.features().isEmpty()) {
                throw new RouteServiceException("Keine Route gefunden");
            }

            DirectionsResponse.Feature feature = response.features().get(0);
            double meters = feature.properties().summary().distance();
            double seconds = feature.properties().summary().duration();

            RouteInfo info = new RouteInfo(
                    Math.round(meters / 10.0) / 100.0,
                    (int) Math.round(seconds / 60.0),
                    serializeGeometry(feature.geometry())
            );
            log.info("Route geladen: [{},{}] → [{},{}] via {} = {} km, {} min",
                    fromLat, fromLng, toLat, toLng, profile, info.distanceKm(), info.durationMinutes());
            return info;
        } catch (RestClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            log.error("ORS error: status={}, body={}", e.getStatusCode(), responseBody);
            throw new RouteServiceException(buildOrsErrorMessage(responseBody, e.getStatusCode().value()), e);
        } catch (RestClientException e) {
            throw new RouteServiceException("Routing fehlgeschlagen für Profil " + profile, e);
        }
    }

    private String buildOrsErrorMessage(String body, int status) {
        String orsMessage = extractOrsErrorMessage(body);
        if (orsMessage == null) {
            return "Routing-Service Fehler (HTTP " + status + ")";
        }
        if (orsMessage.contains(ORS_HINT_DISTANCE_TOO_LONG)) {
            return "Eine Etappe ist zu lang für OpenRouteService "
                    + "(Limit ca. 6000 km für Auto/Fahrrad, 100 km für Fuß). "
                    + "Bitte einen Zwischenstopp einfügen, damit die Etappen kürzer werden.";
        }
        if (orsMessage.contains(ORS_HINT_NOT_ROUTABLE)) {
            return "Ein Punkt liegt nicht am Straßen- bzw. Wegenetz. Bitte einen anderen Ort wählen.";
        }
        return "Routing fehlgeschlagen: " + orsMessage;
    }

    private String extractOrsErrorMessage(String body) {
        if (body == null) return null;
        try {
            JsonNode error = objectMapper.readTree(body).path("error");
            if (error.isString()) return error.asString();
            return error.path("message").asString(null);
        } catch (JacksonException e) {
            log.debug("ORS-Error-Body nicht parsbar als JSON");
            return null;
        }
    }

    private String serializeGeometry(DirectionsResponse.Geometry geom) {
        try {
            return objectMapper.writeValueAsString(geom);
        } catch (JacksonException e) {
            throw new RouteServiceException("Geometrie-Serialisierung fehlgeschlagen", e);
        }
    }

    private String mapToOrsProfile(TransportType type) {
        return switch (type) {
            case WALK -> "foot-walking";
            case HIKING -> "foot-hiking";
            case BIKE -> "cycling-regular";
            case MOUNTAIN_BIKE -> "cycling-mountain";
            case ROAD_BIKE -> "cycling-road";
            case CAR -> "driving-car";
            case MOTORHOME -> "driving-hgv";
        };
    }

    private record DirectionsResponse(List<Feature> features) {
        record Feature(Geometry geometry, Properties properties) {}
        record Geometry(String type, List<List<Double>> coordinates) {}
        record Properties(Summary summary) {}
        record Summary(double distance, double duration) {}
    }
}
