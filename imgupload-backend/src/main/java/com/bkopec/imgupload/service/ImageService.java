package com.bkopec.imgupload.service;

import com.bkopec.imgupload.dto.ImageDto;
import com.bkopec.imgupload.entity.Image;
import com.bkopec.imgupload.exception.ResourceNotFoundException;
import com.bkopec.imgupload.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ImageService {

    @Value("${app.image.upload-dir}")
    private String uploadDir;

    @Value("${app.image.base-url}")
    private String baseUrl;

    private final ImageRepository imageRepository;

    public ImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    // --- File Saving Logic ---
    @Transactional
    public ImageDto uploadImage(MultipartFile file) throws IOException {
        // Ensure upload directory exists
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 1. Generate a short, unique filename to prevent collisions
        // Using UUID.randomUUID() ensures uniqueness, then taking first 8 chars for brevity.
        // It's still highly improbable for a collision.
        // Get file extension from original filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String uniqueFilename = UUID.randomUUID().toString().substring(0, 8) + fileExtension;
        Path filePath = uploadPath.resolve(uniqueFilename);

        // Check for actual collision (highly unlikely with UUID) and regenerate if needed
        int attempts = 0;
        while (Files.exists(filePath) && attempts < 5) { // Try a few times
            uniqueFilename = UUID.randomUUID().toString().substring(0, 8) + fileExtension;
            filePath = uploadPath.resolve(uniqueFilename);
            attempts++;
        }
        if (Files.exists(filePath)) {
            throw new IOException("Failed to generate unique filename after multiple attempts.");
        }

        // 2. Save the file to the specified directory
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // 3. Store metadata in the database
        String publicUrl = baseUrl + uniqueFilename; // Construct public URL
        Image image = new Image(
                uniqueFilename,
                publicUrl,
                originalFilename,
                file.getContentType(),
                file.getSize()
        );
        Image savedImage = imageRepository.save(image);

        return mapToDto(savedImage);
    }

    // --- Image Retrieval ---
    @Transactional(readOnly = true)
    public ImageDto getImageById(Long id) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + id));
        return mapToDto(image);
    }

    @Transactional(readOnly = true)
    public List<ImageDto> getAllImages() {
        return imageRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // --- Image Deletion ---
    @Transactional
    public void deleteImage(Long id) throws IOException {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + id));

        // Delete file from disk
        Path filePath = Paths.get(uploadDir).resolve(image.getFilename());
        Files.deleteIfExists(filePath); // Use deleteIfExists to avoid exception if file is already gone

        // Delete metadata from database
        imageRepository.delete(image);
    }

    // Helper method to map Entity to DTO
    private ImageDto mapToDto(Image image) {
        return new ImageDto(
                image.getId(),
                image.getFilename(),
                image.getPublicUrl(),
                image.getOriginalFilename(),
                image.getContentType(),
                image.getSize(),
                image.getCreatedAt(),
                image.getUpdatedAt()
        );
    }
}