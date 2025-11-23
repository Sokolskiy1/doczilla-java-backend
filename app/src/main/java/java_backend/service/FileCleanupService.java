package java_backend.service;

import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FileCleanupService {
    private final ScheduledExecutorService scheduler;
    private final FileService fileService;

    public FileCleanupService() throws SQLException {
        System.out.println("Initializing FileCleanupService...");
        this.fileService = new FileService();
        this.scheduler = Executors.newScheduledThreadPool(1);

        // Запуск очистки каждые 2 минуты, начиная сразу
        scheduler.scheduleAtFixedRate(this::cleanupOldFiles, 0, 2, TimeUnit.MINUTES);
        System.out.println("FileCleanupService started - will clean up files older than 5 minutes every 2 minutes");
    }

    private void cleanupOldFiles() {
        try {
            System.out.println("=== CLEANUP TASK STARTED ===");
            System.out.println("Starting cleanup of old files...");

            java.util.List<FileService.FileInfo> oldFiles = fileService.getOldFiles(5);

            if (oldFiles.isEmpty()) {
                System.out.println("No old files to clean up");
                return;
            }

            int deletedCount = 0;
            for (FileService.FileInfo fileInfo : oldFiles) {
                boolean deleted = fileService.deleteFile(fileInfo.getUuid());
                if (deleted) {
                    deletedCount++;
                    System.out.println("Cleaned up file: " + fileInfo.getUuid() +
                                     " (created at: " + fileInfo.getStartTime() + ")");
                }
            }

            System.out.println("Cleanup completed. Deleted " + deletedCount + " old files");

        } catch (Exception e) {
            System.err.println("Error during file cleanup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            System.out.println("Shutting down FileCleanupService...");
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
