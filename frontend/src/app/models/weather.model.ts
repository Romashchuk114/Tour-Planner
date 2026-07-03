export interface DailyForecast {
  date: string;
  minTemp: number;
  maxTemp: number;
  rainProbability: number;
  description: string;
}

export interface Weather {
  temperature: number;
  windSpeed: number;
  description: string;
  forecast: DailyForecast[];
}
