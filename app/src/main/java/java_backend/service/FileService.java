package java_backend.service;
import java.util.UUID;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.apache.commons.io.IOUtils;

import java_backend.connector.ConnectorBD;

public class FileService {
    private final ConnectorBD connectorBD;

    public FileService() throws SQLException {
        this.connectorBD = new ConnectorBD();
        // createFilesTableIfNotExists();
    }

    public boolean addingFile(String uuid_file, String uuid_user, String file_type) {
        String insertSQL = "INSERT INTO files_exchange (uuid, user_uuid,  start_time) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = connectorBD.getDatabaseConnector().prepareStatement(insertSQL)) {
            pstmt.setString(1, uuid_file);
            pstmt.setString(2, uuid_user);
            pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error adding file to database: " + e.getMessage());
            return false;
        }
    }

    

}
