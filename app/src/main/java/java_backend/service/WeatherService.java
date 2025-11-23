package java_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WeatherService {
    private final Jedis jedis;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private static final int CACHE_TTL_SECONDS = 15 * 60; // 15 minutes
    private static final String GEOCODING_API_URL = "https://geocoding-api.open-meteo.com/v1/search?name=";
    private static final String WEATHER_API_URL = "https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f&hourly=temperature_2m&timezone=auto";

    public WeatherService() {
        this.jedis = new Jedis("localhost", 6379);
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        // Configure ObjectMapper for LocalDateTime
        this.objectMapper.findAndRegisterModules();
    }

    public WeatherData getWeatherData(String city) throws IOException, InterruptedException {
        String cacheKey = "weather:" + city.toLowerCase();

        // Try to get from cache first
        try {
            String cachedData = jedis.get(cacheKey);
            if (cachedData != null) {
                try {
                    WeatherData weatherData = objectMapper.readValue(cachedData, WeatherData.class);
                    // Check if cache is still valid (within 15 minutes)
                    if (weatherData.getCachedAt().plusMinutes(15).isAfter(LocalDateTime.now())) {
                        return weatherData;
                    }
                } catch (Exception e) {
                    // Cache corrupted, continue to fetch new data
                    System.err.println("Error reading cached weather data: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            // Redis connection failed, continue to fetch new data
            System.err.println("Redis connection failed, fetching fresh data: " + e.getMessage());
        }

        // Fetch new data
        WeatherData weatherData = fetchWeatherData(city);

        // Cache the data (don't fail if caching fails)
        try {
            String jsonData = objectMapper.writeValueAsString(weatherData);
            jedis.setex(cacheKey, CACHE_TTL_SECONDS, jsonData);
        } catch (Exception e) {
            System.err.println("Error caching weather data: " + e.getMessage());
            // Continue anyway - we have the data
        }

        return weatherData;
    }

    private WeatherData fetchWeatherData(String city) throws IOException, InterruptedException {
        // Step 1: Get coordinates
        Coordinates coordinates = getCoordinates(city);

        // Step 2: Get weather forecast
        return getWeatherForecast(city, coordinates.latitude, coordinates.longitude);
    }

    private Coordinates getCoordinates(String city) throws IOException, InterruptedException {
        String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
        String url = GEOCODING_API_URL + encodedCity;
        System.out.println("Geocoding URL: " + url);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Geocoding API returned status: " + response.statusCode());
        }

        JsonNode rootNode = objectMapper.readTree(response.body());
        JsonNode results = rootNode.get("results");

        if (results == null || results.size() == 0) {
            throw new IOException("City not found: " + city);
        }

        JsonNode firstResult = results.get(0);
        double latitude = firstResult.get("latitude").asDouble();
        double longitude = firstResult.get("longitude").asDouble();

        System.out.println("Geocoding result for " + city + ": lat=" + latitude + ", lon=" + longitude);

        return new Coordinates(latitude, longitude);
    }

    private WeatherData getWeatherForecast(String city, double latitude, double longitude)
            throws IOException, InterruptedException {

        String url = String.format(Locale.US, "https://api.open-meteo.com/v1/forecast?latitude=%.6f&longitude=%.6f&hourly=temperature_2m&timezone=Europe/Moscow",
                latitude, longitude);
        System.out.println("Weather API URL: " + url);
        System.out.println("Coordinates: lat=" + latitude + ", lon=" + longitude);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.out.println("Weather API response body: " + response.body());
            throw new IOException("Weather API returned status: " + response.statusCode());
        }

        JsonNode rootNode = objectMapper.readTree(response.body());
        JsonNode hourly = rootNode.get("hourly");

        if (hourly == null) {
            throw new IOException("Invalid weather API response");
        }

        JsonNode timeArray = hourly.get("time");
        JsonNode temperatureArray = hourly.get("temperature_2m");

        List<WeatherData.HourlyWeather> hourlyWeather = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

        // Get next 24 hours (assuming API returns hourly data)
        int hoursToShow = Math.min(24, timeArray.size());

        for (int i = 0; i < hoursToShow; i++) {
            String timeStr = timeArray.get(i).asText();
            LocalDateTime time = LocalDateTime.parse(timeStr, formatter);
            double temperature = temperatureArray.get(i).asDouble();

            hourlyWeather.add(new WeatherData.HourlyWeather(time, temperature));
        }

        return new WeatherData(city, latitude, longitude, hourlyWeather);
    }

    public void close() {
        if (jedis != null) {
            jedis.close();
        }
    }

    private static class Coordinates {
        double latitude;
        double longitude;

        Coordinates(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}
