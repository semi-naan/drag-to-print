package com.dragnprint.io;

import com.dragnprint.model.DocumentType;
import com.dragnprint.preview.RenderedDocument;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public final class DocumentReaders {
    private final List<DocumentReader> readers;

    public DocumentReaders() {
        this(defaultReaders());
    }

    public DocumentReaders(List<DocumentReader> readers) {
        this.readers = List.copyOf(Objects.requireNonNull(readers, "readers"));
    }

    public static List<DocumentReader> defaultReaders() {
        return List.of(
                new PdfDocumentReader(),
                new ImageDocumentReader(),
                new TextDocumentReader(),
                new WordDocumentReader(),
                new LegacyWordDocumentReader(),
                new OdtDocumentReader());
    }

    public RenderedDocument read(Path file) throws IOException {
        Objects.requireNonNull(file, "file");
        DocumentType type = DocumentType.classify(file);
        if (!type.isSupported()) {
            throw new UnsupportedDocumentException("Unsupported file type: " + file.getFileName());
        }
        for (DocumentReader reader : readers) {
            if (reader.supports(type)) {
                return reader.read(file);
            }
        }
        throw new UnsupportedDocumentException("No reader available for " + type);
    }
}
