package com.dragnprint.io;

import com.dragnprint.model.DocumentType;
import com.dragnprint.preview.RenderedDocument;
import com.dragnprint.preview.RenderedPage;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;

public final class ImageDocumentReader implements DocumentReader {
    @Override
    public boolean supports(DocumentType type) {
        return type == DocumentType.IMAGE;
    }

    @Override
    public RenderedDocument read(Path file) throws IOException {
        BufferedImage image = ImageIO.read(file.toFile());
        if (image == null) {
            throw new IOException("Unsupported or corrupt image: " + file.getFileName());
        }
        return new RenderedDocument(file.getFileName().toString(), DocumentType.IMAGE,
                List.of(RenderedPage.ofImage(image)));
    }
}
