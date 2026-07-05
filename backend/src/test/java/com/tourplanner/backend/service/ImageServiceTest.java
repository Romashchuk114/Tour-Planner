package com.tourplanner.backend.service;

import com.tourplanner.backend.service.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ImageServiceTest {

    @TempDir
    Path tempDir;

    private ImageService imageService;

    @BeforeEach
    void setUp() throws IOException {
        imageService = new ImageService(tempDir.toString());
    }

    private byte[] pngBytes() throws IOException {
        BufferedImage image = new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }

    @Test
    void saveAndLoadRoundtripWorks() throws IOException {
        String filename = imageService.save(7L, pngBytes(), "foto.png");

        assertEquals("tour_7.png", filename);
        Resource resource = imageService.load(filename);
        assertTrue(resource.exists());
        assertArrayEquals(pngBytes(), resource.getContentAsByteArray());
    }

    @Test
    void saveRejectsDisallowedExtension() {
        assertThrows(IllegalArgumentException.class,
                () -> imageService.save(7L, new byte[]{1, 2, 3}, "malware.exe"));
    }

    @Test
    void saveRejectsNonImageContent() {
        assertThrows(IllegalArgumentException.class,
                () -> imageService.save(7L, "kein bild".getBytes(), "fake.png"));
    }

    @Test
    void deleteRemovesFileAndLoadFailsAfterwards() throws IOException {
        String filename = imageService.save(7L, pngBytes(), "foto.png");

        imageService.delete(filename);

        assertThrows(ResourceNotFoundException.class, () -> imageService.load(filename));
    }
}
