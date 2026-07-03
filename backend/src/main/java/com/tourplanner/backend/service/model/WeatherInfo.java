package com.tourplanner.backend.service.model;

import java.util.List;

public record WeatherInfo(
        double temperature,
        double windSpeed,
        String description,
        List<DailyForecast> forecast
) {
    public record DailyForecast(
            String date,
            double minTemp,
            double maxTemp,
            int rainProbability,
            String description
    ) {}
}
