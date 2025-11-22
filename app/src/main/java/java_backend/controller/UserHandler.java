package java_backend.controller;
import com.sun.net.httpserver.HttpHandler;

import java_backend.connector.ConnectorBD;
import java.sql.*;
import com.sun.net.httpserver.HttpExchange;
import java_backend.service.UserService;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.io.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UserHandler implements HttpHandler {

    private UserService userService;

    public UserHandler() {
        try {
            this.userService = new UserService();
        } catch (SQLException e) {
            System.err.println("Ошибка при инициализации UserService: " + e.getMessage());
            throw new RuntimeException("Не удалось инициализировать обработчик пользователей", e);
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.print("System.out.print(параметр)System.out.print(параметр)");
        try {
            switch (exchange.getRequestMethod()) {
                case "POST":
                    handleCreateUser(exchange);
                    break;
                case "GET":
                    
                    break;
                default:
                    sendResponse(exchange, 405, "Method Not Allowed");
            }
        } catch (Exception e) {
            // sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
    private void handleCreateUser(HttpExchange exchange){
        try {
            InputStream requestBody = exchange.getRequestBody();
            String body = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(body);

            String login = jsonNode.get("login").asText();
            String password = jsonNode.get("password").asText();

            boolean userCreated = userService.createUser(login, password);
            if (userCreated) {
                String response = "{\"message\": \"Пользователь создан успешно\"}";
                sendResponse(exchange, 201, response);
            } else {
                String response = "{\"error\": \"Пользователь с таким логином уже существует\"}";
                sendResponse(exchange, 409, response);
            }

        } catch (IOException e) {
            System.err.println("Ошибка чтения тела запроса: " + e.getMessage());
            try {
                sendResponse(exchange, 400, "{\"error\": \"Ошибка чтения данных запроса\"}");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("Ошибка обработки JSON: " + e.getMessage());
            try {
                sendResponse(exchange, 400, "{\"error\": \"Ошибка обработки данных: " + e.getMessage() + "\"}");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    // private void handleCreateUser(HttpExchange exchange){
    //     String response = "gg";
    //     String sql = "SELECT * FROM users";
    //     System.out.println("Start sql");
    //     InputStream requestBody = exchange.getRequestBody();
    //     String body = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);
        
    //     // Парсим JSON
    //     ObjectMapper objectMapper = new ObjectMapper();
    //     JsonNode jsonNode = objectMapper.readTree(body);
        
    //     // Извлекаем логин и пароль
    //     String login = jsonNode.get("login").asText();
    //     String password = jsonNode.get("password").asText();
        
    //     System.out.println("Получен логин: " + login);
    //     System.out.println("Получен пароль: " + password);
        
    //     // Теперь вы можете использовать login и password для работы с БД
    //     // Например, для проверки или создания пользователя
        
    //     String response = "Пользователь обработан: " + login;
    //     sendResponse(exchange, 201, response);
        // try{
        //    ConnectorBD connectorClass  = new ConnectorBD();
        //     System.out.println("Start sql1");
        //    try (Statement stmt = connectorClass.getDatabaseConnector().createStatement();
        //      ResultSet rs = stmt.executeQuery(sql)) {
        //      System.out.println("SQL executed successfully");
            
        //     if (rs.next()) {
        //         System.out.println("Data found in database");
        //         response = "Data exists";
        //     } else {
        //         System.out.println("No data found");
        //         response = "No data";
        //     }
        //     System.out.println("finish sql");
        
        
        // System.out.println("Sending response: " + response);
            
        // }
           
    //        sendResponse(exchange, 201, response); 
    //     }
    //     catch(Exception e){

    //         System.err.println(" Error" + e.getMessage());
    //         e.printStackTrace();
        
    //         response = "Error: " + e.getMessage();
    //     }
        

    // }

}
