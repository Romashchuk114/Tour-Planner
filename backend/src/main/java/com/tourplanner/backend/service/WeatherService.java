package com.tourplanner.backend.service;

import com.tourplanner.backend.service.exception.WeatherServiceException;
import com.tourplanner.backend.service.model.GeoCoordinate;
import com.tourplanner.backend.service.model.WeatherInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WeatherService {

    private final RestClient weatherClient;

    public WeatherService(@Value("${app.weather.base-url}") String baseUrl) {
        this.weatherClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public List<WeatherInfo> fetchWeather(List<GeoCoordinate> points) {
        String latitudes = points.stream().map(p -> String.valueOf(p.lat())).collect(Collectors.joining(","));
        String longitudes = points.stream().map(p -> String.valueOf(p.lng())).collect(Collectors.joining(","));

        try {
            List<OpenMeteoResponse> responses = weatherClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/v1/forecast")
                            .queryParam("latitude", latitudes)
                            .queryParam("longitude", longitudes)
                            .queryParam("current", "temperature_2m,weather_code,wind_speed_10m")
                            .queryParam("daily", "temperature_2m_max,temperature_2m_min,precipitation_probability_max,weather_code")
                            .queryParam("forecast_days", 3)
                            .queryParam("timezone", "auto")
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<OpenMeteoResponse>>() {});

            if (responses == null || responses.size() != points.size()) {
                throw new WeatherServiceException("Unerwartete Antwort vom Wetterdienst", null);
            }

            log.info("Wetter geladen für {} Standorte", points.size());
            return responses.stream().map(this::toWeatherInfo).toList();
        } catch (RestClientException e) {
            throw new WeatherServiceException("Wetterdienst nicht erreichbar", e);
        }
    }

    private WeatherInfo toWeatherInfo(OpenMeteoResponse response) {
        if (response.current() == null || response.daily() == null) {
            throw new WeatherServiceException("Leere Antwort vom Wetterdienst", null);
        }

        List<WeatherInfo.DailyForecast> forecast = new ArrayList<>();
        OpenMeteoResponse.Daily daily = response.daily();
        for (int i = 0; i < daily.time().size(); i++) {
            forecast.add(new WeatherInfo.DailyForecast(
                    daily.time().get(i),
                    daily.temperature_2m_min().get(i),
                    daily.temperature_2m_max().get(i),
                    daily.precipitation_probability_max().get(i),
                    describe(daily.weather_code().get(i))
            ));
        }
        return new WeatherInfo(
                response.current().temperature_2m(),
                response.current().wind_speed_10m(),
                describe(response.current().weather_code()),
                forecast
        );
    }

    private String describe(int weatherCode) {
        return switch (weatherCode) {
            case 0 -> "Klar";
            case 1, 2 -> "Überwiegend sonnig";
            case 3 -> "Bewölkt";
            case 45, 48 -> "Nebel";
            case 51, 53, 55, 56, 57 -> "Nieselregen";
            case 61, 63, 65, 66, 67 -> "Regen";
            case 71, 73, 75, 77 -> "Schneefall";
            case 80, 81, 82 -> "Regenschauer";
            case 85, 86 -> "Schneeschauer";
            case 95, 96, 99 -> "Gewitter";
            default -> "Unbekannt";
        };
    }

    private record OpenMeteoResponse(Current current, Daily daily) {
        record Current(double temperature_2m, int weather_code, double wind_speed_10m) {}

        record Daily(List<String> time, List<Double> temperature_2m_max, List<Double> temperature_2m_min,
                     List<Integer> precipitation_probability_max, List<Integer> weather_code) {}
    }
}
