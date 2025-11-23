package java_backend;

import com.sun.net.httpserver.HttpServer;

import java_backend.controller.UserHandler;
import java_backend.controller.WeatherHandler;
import java_backend.service.FileCleanupService;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.sql.SQLException;

import java_backend.controller.FileHandler;

public class BackendServer {

    // CORS headers utility method
    public static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization, X-File-Type, Accept, X-Requested-With");
        exchange.getResponseHeaders().set("Access-Control-Max-Age", "3600");
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        server.createContext("/api/hello", new HelloHandler());
        server.createContext("/api/user", new UserHandler());
        server.createContext("/api/file", new FileHandler());
        server.createContext("/api/weather", new WeatherHandler());

         FileCleanupService cleanupService = null;
        try {
            cleanupService = new FileCleanupService();
        } catch (SQLException e) {
            System.err.println("Failed to start FileCleanupService: " + e.getMessage());
        }

        final FileCleanupService finalCleanupService = cleanupService;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            if (finalCleanupService != null) {
                finalCleanupService.shutdown();
            }
            server.stop(5);
        }));

        server.start();
        
    }
    static class HelloHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);

            if ("GET".equals(exchange.getRequestMethod())) {
                String response = "Hello my friend!";
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else if ("OPTIONS".equals(exchange.getRequestMethod())) {
                // Handle preflight requests
                addCorsHeaders(exchange);
                exchange.sendResponseHeaders(200, -1);
            }

        }
    }
}

