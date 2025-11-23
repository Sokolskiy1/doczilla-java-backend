package java_backend.service;

import java.time.LocalDateTime;
import java.util.List;

public class WeatherData {
    private String city;
    private double latitude;
    private double longitude;
    private List<HourlyWeather> hourlyWeather;
    private LocalDateTime cachedAt;

    public WeatherData() {}

    public WeatherData(String city, double latitude, double longitude, List<HourlyWeather> hourlyWeather) {
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
        this.hourlyWeather = hourlyWeather;
        this.cachedAt = LocalDateTime.now();
    }

    // Getters and setters
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public List<HourlyWeather> getHourlyWeather() { return hourlyWeather; }
    public void setHourlyWeather(List<HourlyWeather> hourlyWeather) { this.hourlyWeather = hourlyWeather; }

    public LocalDateTime getCachedAt() { return cachedAt; }
    public void setCachedAt(LocalDateTime cachedAt) { this.cachedAt = cachedAt; }

    public static class HourlyWeather {
        private LocalDateTime time;
        private double temperature;

        public HourlyWeather() {}

        public HourlyWeather(LocalDateTime time, double temperature) {
            this.time = time;
            this.temperature = temperature;
        }

        public LocalDateTime getTime() { return time; }
        public void setTime(LocalDateTime time) { this.time = time; }

        public double getTemperature() { return temperature; }
        public void setTemperature(double temperature) { this.temperature = temperature; }
    }
}
