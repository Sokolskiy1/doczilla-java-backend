package java_backend.service;
import java.util.UUID;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

    public FileInfo getFileByUuid(String uuid) {
        String selectSQL = "SELECT uuid, user_uuid, start_time FROM files_exchange WHERE uuid = ?";

        try (PreparedStatement pstmt = connectorBD.getDatabaseConnector().prepareStatement(selectSQL)) {
            pstmt.setString(1, uuid);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new FileInfo(
                        rs.getString("uuid"),
                        rs.getString("user_uuid"),
                        rs.getTimestamp("start_time")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting file by UUID: " + e.getMessage());
        }

        return null;
    }

    public FileDownloadInfo getFileForDownload(String uuid) throws IOException {
        // Проверяем, существует ли файл в базе данных
        FileInfo fileInfo = getFileByUuid(uuid);
        if (fileInfo == null) {
            return null;
        }

        Path uploadDir = Paths.get("uploads/");

        // Ищем файл с указанным UUID
        try (var files = Files.list(uploadDir)) {
            Path filePath = files
                .filter(path -> path.getFileName().toString().startsWith(uuid + "."))
                .findFirst()
                .orElse(null);

            if (filePath != null && Files.exists(filePath)) {
                byte[] fileData = Files.readAllBytes(filePath);
                String fileName = filePath.getFileName().toString();
                String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
                String contentType = getContentTypeByExtension(extension);

                return new FileDownloadInfo(fileData, fileName, contentType, fileInfo);
            }
        }

        return null;
    }

    public byte[] readFileByUuid(String uuid) throws IOException {
        FileDownloadInfo downloadInfo = getFileForDownload(uuid);
        return downloadInfo != null ? downloadInfo.getFileData() : null;
    }

    private String getContentTypeByExtension(String extension) {
        switch (extension.toLowerCase()) {
            case "txt":
                return "text/plain";
            case "pdf":
                return "application/pdf";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            default:
                return "application/octet-stream";
        }
    }

    public static class FileDownloadInfo {
        private final byte[] fileData;
        private final String fileName;
        private final String contentType;
        private final FileInfo fileInfo;

        public FileDownloadInfo(byte[] fileData, String fileName, String contentType, FileInfo fileInfo) {
            this.fileData = fileData;
            this.fileName = fileName;
            this.contentType = contentType;
            this.fileInfo = fileInfo;
        }

        public byte[] getFileData() { return fileData; }
        public String getFileName() { return fileName; }
        public String getContentType() { return contentType; }
        public FileInfo getFileInfo() { return fileInfo; }
    }

    public static class FileInfo {
        private final String uuid;
        private final String userUuid;
        private final Timestamp startTime;

        public FileInfo(String uuid, String userUuid, Timestamp startTime) {
            this.uuid = uuid;
            this.userUuid = userUuid;
            this.startTime = startTime;
        }

        public String getUuid() { return uuid; }
        public String getUserUuid() { return userUuid; }
        public Timestamp getStartTime() { return startTime; }
    }

    

}
