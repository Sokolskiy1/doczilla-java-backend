package java_backend;

import com.sun.net.httpserver.HttpServer;

import java_backend.controller.UserHandler;
import java_backend.service.FileCleanupService;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.sql.SQLException;

import java_backend.controller.FileHandler;

public class BackendServer {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        server.createContext("/api/hello", new HelloHandler());
        server.createContext("/api/user", new UserHandler());
        server.createContext("/api/file", new FileHandler());

         FileCleanupService cleanupService = null;
        try {
            cleanupService = new FileCleanupService();
        } catch (SQLException e) {
            System.err.println("Failed to start FileCleanupService: " + e.getMessage());
        }

        // Add shutdown hook to properly stop the cleanup service
        final FileCleanupService finalCleanupService = cleanupService;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            if (finalCleanupService != null) {
                finalCleanupService.shutdown();
            }
            server.stop(5);
        }));

        server.start();
        // Add shutdown hook to properly stop the cleanup service
        
    }
    static class HelloHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String response = "Hello my friend!";
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
            
        }
    }
}

