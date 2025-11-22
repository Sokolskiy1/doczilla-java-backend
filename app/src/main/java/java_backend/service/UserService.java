package java_backend.service;

import java_backend.connector.ConnectorBD;
import java.sql.*;
import java.util.Optional;
import java.util.UUID;
public class UserService {
    private final ConnectorBD connectorBD;

    public UserService() throws SQLException {
        this.connectorBD = new ConnectorBD();
    }

    public boolean createUser(String login, String password) {
        if (login == null || login.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Логин и пароль не могут быть пустыми");
        }
        String uuid_user  = UUID.randomUUID().toString();
        String insertSQL = "INSERT INTO users (login, password, uuid) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = connectorBD.getDatabaseConnector().prepareStatement(insertSQL)) {
            pstmt.setString(1, login.trim());
            pstmt.setString(2, password); 
            pstmt.setString(3, uuid_user);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                return false;
            } else {
                throw new RuntimeException("Failed to create a user", e);
            }
        }
    }
    
    public void close() {
        try {
            if (connectorBD.getDatabaseConnector() != null && !connectorBD.getDatabaseConnector().isClosed()) {
                connectorBD.getDatabaseConnector().close();
            }
        } catch (SQLException e) {
            System.err.println("Error: close bd " + e.getMessage());
        }
    }

 
    public static class User {
        private final UUID uuid;
        private final String login;
        private final String password;
        private final Timestamp createdAt;

        public User(UUID uuid, String login, String password, Timestamp createdAt) {
            this.uuid = uuid;
            this.login = login;
            this.password = password;
            this.createdAt = createdAt;
        }

        public UUID getId() { return uuid; }
        public String getLogin() { return login; }
        public String getPassword() { return password; }
        public Timestamp getCreatedAt() { return createdAt; }
    }
}
