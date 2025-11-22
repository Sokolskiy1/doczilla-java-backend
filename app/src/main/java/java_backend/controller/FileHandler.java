package java_backend.controller;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java_backend.service.FileService;

public class FileHandler implements HttpHandler{
    private FileService filerService;

    public FileHandler() {
        try {
            this.filerService = new FileService();
            File uploadDir = new File("uploads");
    if (!uploadDir.exists()) {
        uploadDir.mkdirs();
    }
        } catch (Exception e) {
            // throw new RuntimeException("Не удалось инициализировать обработчик пользователей", e);SQLException
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
    private void handleFileUpload(HttpExchange exchange){

        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        // String boundary = extractBoundary(contentType);
        System.out.print(exchange.getRequestHeaders());
        if (contentType == null || !contentType.startsWith("multipart/form-data")) {
            try {
                sendResponse(exchange, 400, "Expected multipart/form-data");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

    boolean status_adding = filerService.addingFile(contentType);


    }

private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

}
