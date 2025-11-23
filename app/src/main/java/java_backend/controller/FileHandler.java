package java_backend.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Base64;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java_backend.service.FileService;
import java_backend.service.FileProcessor;
import java_backend.service.UserService;

public class FileHandler implements HttpHandler{
    private FileService fileService;
    private FileProcessor fileProcessor;
    private UserService userService;

    public FileHandler() {
        try {
            this.fileService = new FileService();
            this.fileProcessor = new FileProcessor();
            this.userService = new UserService();
        } catch (Exception e) {
            System.err.println("Failed to initialize FileHandler: " + e.getMessage());
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            switch (exchange.getRequestMethod()) {
                case "POST":
                   handleFileUpload(exchange);
                    break;
                case "GET":
                    
                    break;
                default:
                    sendResponse(exchange, 405, "Method Not Allowed");
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }
    private void handleFileUpload(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            // Проверяем аутентификацию пользователя
            String userId = authenticateUser(exchange);
            if (userId == null) {
                sendResponse(exchange, 401, "Unauthorized: Неверные учетные данные");
                return;
            }

            String fileType = exchange.getRequestHeaders().getFirst("X-File-Type");
            String uuid = fileProcessor.processFileUpload(exchange,fileType);

            if (uuid!=null) {
                boolean fileSaved = fileService.addingFile(uuid, userId, fileType);
                if (fileSaved) {
                    sendResponse(exchange, 201, "ok: Файл успешно загружен и сохранен в БД пользователем " + userId);
                } else {
                    sendResponse(exchange, 500, "Internal Server Error: Файл загружен, но не удалось сохранить в БД");
                }
            } else {

               sendResponse(exchange, 500, "Internal Server Error: Ошибка при загрузке файла" );
            }
        } else {

            exchange.sendResponseHeaders(405, -1);
        }
    }

    private String authenticateUser(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");

        if (authHeader != null && authHeader.startsWith("Basic ")) {
            try {
                String base64Credentials = authHeader.substring("Basic ".length());
                String credentials = new String(Base64.getDecoder().decode(base64Credentials));

                String[] parts = credentials.split(":", 2);
                if (parts.length == 2) {
                    String login = parts[0];
                    String password = parts[1];
                    return userService.authenticateUser(login, password);
                }
            } catch (Exception e) {
                System.err.println("Ошибка при Basic аутентификации: " + e.getMessage());
            }
        }

        return null;
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

}
