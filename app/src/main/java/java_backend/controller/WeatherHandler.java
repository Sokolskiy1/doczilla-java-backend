package java_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java_backend.service.WeatherData;
import java_backend.service.WeatherService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class WeatherHandler implements HttpHandler {

    private final WeatherService weatherService;
    private final ObjectMapper objectMapper;

    public WeatherHandler() {
        this.weatherService = new WeatherService();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\": \"Method Not Allowed\"}");
                return;
            }

            // Parse query parameters
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQueryParams(query);

            String city = params.get("city");
            if (city == null || city.trim().isEmpty()) {
                sendResponse(exchange, 400, "{\"error\": \"City parameter is required\"}");
                return;
            }

            // Decode city name
            city = URLDecoder.decode(city, StandardCharsets.UTF_8);

            // Get weather data
            WeatherData weatherData = weatherService.getWeatherData(city);

            // Convert to JSON and send response
            String jsonResponse = objectMapper.writeValueAsString(weatherData);
            sendResponse(exchange, 200, jsonResponse);

        } catch (IOException e) {
            System.err.println("IO Error in WeatherHandler: " + e.getMessage());
            sendResponse(exchange, 500, "{\"error\": \"Internal Server Error\"}");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            sendResponse(exchange, 500, "{\"error\": \"Request interrupted\"}");
        } catch (Exception e) {
            System.err.println("Error in WeatherHandler: " + e.getMessage());
            sendResponse(exchange, 500, "{\"error\": \"Internal Server Error\"}");
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes(StandardCharsets.UTF_8));
        os.close();
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) {
            return params;
        }

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String key = pair.substring(0, idx);
                String value = pair.substring(idx + 1);
                params.put(key, value);
            }
        }
        return params;
    }
}
