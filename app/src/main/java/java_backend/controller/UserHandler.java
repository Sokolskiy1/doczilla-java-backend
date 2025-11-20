package java_backend.controller;
import com.sun.net.httpserver.HttpHandler;

import java_backend.connector.ConnectorBD;
import java.sql.*;
import com.sun.net.httpserver.HttpExchange;
// import java_backend.service.UserService;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.*;
public class UserHandler implements HttpHandler {

    // private UserService userService;
    
    // public UserHandler(UserService userService) {
    //     this.userService = userService;
    // }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.print("System.out.print(параметр)System.out.print(параметр)");
        try {
            switch (exchange.getRequestMethod()) {
                case "POST":
                    handleCreateUser(exchange);
                    break;
                case "GET":
                    String response = "Hello usergg!";
                   

                    System.out.print("System.out.print(параметр)System.out.print(параметр)");
                   
                    sendResponse(exchange, 200, response);
                  
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
        String response = "gg";
        String sql = "SELECT * FROM users";
        System.out.println("Start sql");
        try{
           ConnectorBD connectorClass  = new ConnectorBD();
            System.out.println("Start sql1");
           try (Statement stmt = connectorClass.getDatabaseConnector().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
             System.out.println("SQL executed successfully");
            
            if (rs.next()) {
                System.out.println("Data found in database");
                response = "Data exists";
            } else {
                System.out.println("No data found");
                response = "No data";
            }
            System.out.println("finish sql");
        
        
        System.out.println("Sending response: " + response);
            
        }
           
           sendResponse(exchange, 201, response); 
        }
        catch(Exception e){

            System.err.println(" Error" + e.getMessage());
            e.printStackTrace();
        
            response = "Error: " + e.getMessage();
        }
        

    }

}
