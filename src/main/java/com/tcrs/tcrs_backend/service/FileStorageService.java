package com.tcrs.tcrs_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Value("${app.upload.max-file-size:10485760}") // 10MB default
    private long maxFileSize;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "pdf", "jpg", "jpeg", "png", "doc", "docx", "xls", "xlsx"
    );

    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "application/pdf",
            "image/jpeg",
            "image/png",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    public FileUploadResult storeFile(MultipartFile file, String subfolder) throws IOException {
        try {
            // Validate file
            validateFile(file);

            // Create upload directory if it doesn't exist
            Path uploadPath = createUploadDirectory(subfolder);

            // Generate unique filename
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = getFileExtension(originalFilename);
            String storedFilename = generateUniqueFilename(fileExtension);

            // Store file
            Path targetLocation = uploadPath.resolve(storedFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            logger.info("File stored successfully: {}", targetLocation);

            return new FileUploadResult(
                    originalFilename,
                    storedFilename,
                    targetLocation.toString(),
                    file.getSize(),
                    file.getContentType()
            );

        } catch (IOException ex) {
            logger.error("Could not store file {}. Please try again!", file.getOriginalFilename(), ex);
            throw new IOException("Could not store file " + file.getOriginalFilename() + ". Please try again!", ex);
        }
    }

    public void deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);
            logger.info("File deleted successfully: {}", filePath);
        } catch (IOException ex) {
            logger.error("Could not delete file: {}", filePath, ex);
        }
    }

    public byte[] loadFileAsBytes(String filePath) throws IOException {
        try {
            Path path = Paths.get(filePath);
            return Files.readAllBytes(path);
        } catch (IOException ex) {
            logger.error("Could not read file: {}", filePath, ex);
            throw new IOException("Could not read file: " + filePath, ex);
        }
    }

    public boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    private void validateFile(MultipartFile file) throws IOException {
        // Check if file is empty
        if (file.isEmpty()) {
            throw new IOException("Cannot store empty file");
        }

        // Check file size
        if (file.getSize() > maxFileSize) {
            throw new IOException("File size exceeds maximum allowed size of " + maxFileSize + " bytes");
        }

        // Check file extension
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = getFileExtension(filename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IOException("File type not allowed. Allowed types: " + ALLOWED_EXTENSIONS);
        }

        // Check MIME type
        String mimeType = file.getContentType();
        if (mimeType == null || !ALLOWED_MIME_TYPES.contains(mimeType)) {
            throw new IOException("MIME type not allowed. File type: " + mimeType);
        }

        // Check for path traversal attack
        if (filename.contains("..")) {
            throw new IOException("Filename contains invalid path sequence: " + filename);
        }
    }

    private Path createUploadDirectory(String subfolder) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String datePath = LocalDateTime.now().format(formatter);
        String fullPath = subfolder != null ? uploadDir + "/" + subfolder + "/" + datePath : uploadDir + "/" + datePath;

        Path uploadPath = Paths.get(fullPath).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        return uploadPath;
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    private String generateUniqueFilename(String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return timestamp + "_" + uuid + "." + extension;
    }

    // Inner class for file upload result
    public static class FileUploadResult {
        private final String originalFilename;
        private final String storedFilename;
        private final String filePath;
        private final long fileSize;
        private final String mimeType;

        public FileUploadResult(String originalFilename, String storedFilename, String filePath, long fileSize, String mimeType) {
            this.originalFilename = originalFilename;
            this.storedFilename = storedFilename;
            this.filePath = filePath;
            this.fileSize = fileSize;
            this.mimeType = mimeType;
        }

        // Getters
        public String getOriginalFilename() { return originalFilename; }
        public String getStoredFilename() { return storedFilename; }
        public String getFilePath() { return filePath; }
        public long getFileSize() { return fileSize; }
        public String getMimeType() { return mimeType; }
    }
}