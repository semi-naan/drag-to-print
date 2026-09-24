package com.dragnprint.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dragnprint.model.DocumentType;
import com.dragnprint.preview.RenderedDocument;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ImageDocumentReaderTest {
    @TempDir
    Path tempDir;

    @Test
    void readsPngImage() throws IOException {
        Path file = tempDir.resolve("picture.png");
        BufferedImage image = new BufferedImage(30, 20, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.BLUE);
        graphics.fillRect(0, 0, 30, 20);
        graphics.dispose();
        ImageIO.write(image, "png", file.toFile());

        RenderedDocument document = new ImageDocumentReader().read(file);

        assertEquals(DocumentType.IMAGE, document.type());
        assertEquals(1, document.pageCount());
        assertTrue(document.page(0).hasImage());
        assertEquals(30, document.page(0).width());
        assertEquals(20, document.page(0).height());
    }

    @Test
    void readsJpegImage() throws IOException {
        Path file = tempDir.resolve("photo.jpg");
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(image, "jpg", file.toFile());

        RenderedDocument document = new ImageDocumentReader().read(file);

        assertEquals(DocumentType.IMAGE, document.type());
        assertEquals(16, document.page(0).width());
    }

    @Test
    void rejectsCorruptImage() throws IOException {
        Path file = tempDir.resolve("broken.png");
        Files.writeString(file, "this is not an image", StandardCharsets.UTF_8);

        assertThrows(IOException.class, () -> new ImageDocumentReader().read(file));
    }

    @Test
    void supportsOnlyImageType() {
        ImageDocumentReader reader = new ImageDocumentReader();
        assertTrue(reader.supports(DocumentType.IMAGE));
        assertEquals(false, reader.supports(DocumentType.TEXT));
    }
}
