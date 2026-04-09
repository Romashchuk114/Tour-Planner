package com.tourplanner.backend.service;

import com.tourplanner.backend.service.exception.ImageStorageException;
import com.tourplanner.backend.service.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

@Slf4j
@Service
public class ImageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final Path baseDir;

    public ImageService(@Value("${app.images.base-dir}") String baseDir) throws IOException {
        this.baseDir = Path.of(baseDir);
        Files.createDirectories(this.baseDir);
    }

    public String save(Long tourId, byte[] data, String originalFilename) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Datei ist leer");
        }

        String extension = getExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Dateityp nicht erlaubt: " + extension + ". Erlaubt: " + ALLOWED_EXTENSIONS);
        }

        String filename = "tour_" + tourId + "." + extension;
        Path filePath = baseDir.resolve(filename);

        try {
            Files.write(filePath, data);
            log.info("Image saved: {}", filename);
            return filename;
        } catch (IOException e) {
            throw new ImageStorageException("Bild konnte nicht gespeichert werden", e);
        }
    }

    public Resource load(String filename) {
        Path filePath = baseDir.resolve(filename);
        Resource resource = new FileSystemResource(filePath);
        if (!resource.exists()) {
            throw new ResourceNotFoundException("Bild nicht gefunden: " + filename);
        }
        return resource;
    }

    public void delete(String filename) {
        if (filename == null) return;
        try {
            Files.deleteIfExists(baseDir.resolve(filename));
            log.info("Image deleted: {}", filename);
        } catch (IOException e) {
            log.warn("Image could not be deleted: {}", filename, e);
        }
    }

    private String getExtension(String filename) {
        if (filename == null) {
            throw new IllegalArgumentException("Dateiname fehlt");
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex == -1) {
            throw new IllegalArgumentException("Keine Dateiendung vorhanden");
        }
        return filename.substring(dotIndex + 1).toLowerCase();
    }
}