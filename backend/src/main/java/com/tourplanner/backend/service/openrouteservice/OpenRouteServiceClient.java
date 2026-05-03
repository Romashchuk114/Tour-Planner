package com.tourplanner.backend.service.openrouteservice;

import com.tourplanner.backend.service.exception.RouteServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class OpenRouteServiceClient {

    private final RestClient orsClient;
    private final String apiKey;

    public OpenRouteServiceClient(@Value("${app.openrouteservice.base-url}") String orsBaseUrl,
                                  @Value("${app.openrouteservice.api-key}") String apiKey) {
        this.apiKey = apiKey;
        this.orsClient = RestClient.builder().baseUrl(orsBaseUrl).build();
    }

    public RouteInfo directions(String profile, double fromLat, double fromLng, double toLat, double toLng) {
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

            return new RouteInfo(
                    Math.round(meters / 10.0) / 100.0,
                    (int) Math.round(seconds / 60.0),
                    buildGeometryJson(feature.geometry())
            );
        } catch (RestClientResponseException e) {
            log.error("ORS directions error: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RouteServiceException("Routing-Service Fehler (HTTP " + e.getStatusCode() + ")", e);
        } catch (RestClientException e) {
            throw new RouteServiceException("Routing fehlgeschlagen für Profil " + profile, e);
        }
    }

    private String buildGeometryJson(DirectionsResponse.Geometry geom) {
        String coords = geom.coordinates().stream()
                .map(c -> "[" + c.get(0) + "," + c.get(1) + "]")
                .collect(Collectors.joining(","));
        return "{\"type\":\"" + geom.type() + "\",\"coordinates\":[" + coords + "]}";
    }

    public record DirectionsResponse(List<Feature> features) {
        public record Feature(Geometry geometry, Properties properties) {}
        public record Geometry(String type, List<List<Double>> coordinates) {}
        public record Properties(Summary summary) {}
        public record Summary(double distance, double duration) {}
    }
}
