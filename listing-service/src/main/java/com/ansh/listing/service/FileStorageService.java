package com.ansh.listing.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Service for handling file uploads and storage.
 * Stores images locally and returns URLs for accessing them.
 */
@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${file.upload-dir:uploads/images}")
    private String uploadDir;

    @Value("${file.base-url:http://localhost:8083/images}")
    private String baseUrl;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private Path uploadPath;

    @PostConstruct
    public void init() {
        try {
            uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);
            logger.info("File upload directory initialized at: {}", uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + uploadDir, e);
        }
    }

    /**
     * Store an uploaded image file and return its URL.
     *
     * @param file The uploaded file
     * @param listingId The listing ID (used for organizing files)
     * @return The URL to access the uploaded image
     * @throws IllegalArgumentException if file is invalid
     * @throws IOException if file cannot be stored
     */
    public String storeImage(MultipartFile file, String listingId) throws IOException {
        // Validate file
        validateFile(file);

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String newFilename = listingId + "_" + UUID.randomUUID().toString().substring(0, 8) + "." + extension;

        // Create listing subdirectory if needed
        Path listingDir = uploadPath.resolve(listingId);
        Files.createDirectories(listingDir);

        // Store file
        Path targetPath = listingDir.resolve(newFilename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        logger.info("Stored image for listing {}: {}", listingId, newFilename);

        // Return URL
        return baseUrl + "/" + listingId + "/" + newFilename;
    }

    /**
     * Delete an image file.
     *
     * @param imageUrl The URL of the image to delete
     * @return true if deleted, false if not found
     */
    public boolean deleteImage(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith(baseUrl)) {
            return false; // Not a local image
        }

        try {
            String relativePath = imageUrl.substring(baseUrl.length() + 1);
            Path filePath = uploadPath.resolve(relativePath);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                logger.info("Deleted image: {}", relativePath);
                return true;
            }
        } catch (IOException e) {
            logger.warn("Failed to delete image: {}", e.getMessage());
        }

        return false;
    }

    /**
     * Get the path to a stored image.
     *
     * @param relativePath The relative path from the image URL
     * @return The full path to the file, or null if not found
     */
    public Path getImagePath(String relativePath) {
        Path filePath = uploadPath.resolve(relativePath).normalize();

        // Security check: ensure path is within upload directory
        if (!filePath.startsWith(uploadPath)) {
            logger.warn("Attempted path traversal attack: {}", relativePath);
            return null;
        }

        if (Files.exists(filePath)) {
            return filePath;
        }

        return null;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed (5MB)");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException("Invalid filename");
        }

        String extension = getFileExtension(filename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("File type not allowed. Allowed types: " + ALLOWED_EXTENSIONS);
        }

        // Validate content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
