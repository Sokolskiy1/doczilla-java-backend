package java_backend.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.sun.net.httpserver.HttpExchange;

public class FileProcessor {
    private static final String UPLOAD_DIR = "uploads/";

   
    public String processFileUpload(HttpExchange exchange,String fileType) throws IOException {
        Files.createDirectories(Paths.get(UPLOAD_DIR));
        String uuid= java.util.UUID.randomUUID().toString();
        String fileName = uuid +"."+ fileType;
        Path filePath = Paths.get(UPLOAD_DIR + fileName);

        try (InputStream inputStream = exchange.getRequestBody();
             FileOutputStream outputStream = new FileOutputStream(filePath.toFile())) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytes = 0;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }

            return uuid;

        } catch (IOException e) {
            return null;
        }
    }

}
