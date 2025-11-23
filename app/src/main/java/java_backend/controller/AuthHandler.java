package java_backend.controller;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java_backend.service.UserService;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AuthHandler implements HttpHandler {

    private UserService userService;

    public AuthHandler() {
        try {
            this.userService = new UserService();
        } catch (Exception e) {
            System.err.println("Ошибка при инициализации UserService: " + e.getMessage());
            throw new RuntimeException("Не удалось инициализировать обработчик аутентификации", e);
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            switch (exchange.getRequestMethod()) {
                case "OPTIONS":
                    // Handle preflight requests
                    sendResponse(exchange, 200, "");
                    break;
                case "POST":
                    handleLogin(exchange);
                    break;
                default:
                    sendResponse(exchange, 405, "Method Not Allowed");
            }
        } catch (Exception e) {
            System.err.println("Ошибка обработки запроса: " + e.getMessage());
            sendResponse(exchange, 500, "{\"error\": \"Внутренняя ошибка сервера\"}");
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        try {
            InputStream requestBody = exchange.getRequestBody();
            String body = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(body);

            String login = jsonNode.get("login").asText();
            String password = jsonNode.get("password").asText();

            String uuid = userService.authenticateUser(login, password);

            if (uuid != null) {
                String response = String.format("{\"message\": \"Аутентификация успешна\", \"uuid\": \"%s\"}", uuid);
                sendResponse(exchange, 200, response);
            } else {
                String response = "{\"error\": \"Неверный логин или пароль\"}";
                sendResponse(exchange, 401, response);
            }

        } catch (IOException e) {
            System.err.println("Ошибка чтения тела запроса: " + e.getMessage());
            sendResponse(exchange, 400, "{\"error\": \"Ошибка чтения данных запроса\"}");
        } catch (Exception e) {
            System.err.println("Ошибка обработки JSON: " + e.getMessage());
            sendResponse(exchange, 400, "{\"error\": \"Ошибка обработки данных: " + e.getMessage() + "\"}");
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        java_backend.BackendServer.addCorsHeaders(exchange);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes(StandardCharsets.UTF_8));
        os.close();
    }
}
