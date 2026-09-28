package com.dragnprint.io;

import com.dragnprint.model.DocumentType;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public final class DocumentWriters {
    private final List<DocumentWriter> writers;

    public DocumentWriters() {
        this(defaultWriters());
    }

    public DocumentWriters(List<DocumentWriter> writers) {
        this.writers = List.copyOf(Objects.requireNonNull(writers, "writers"));
    }

    public static List<DocumentWriter> defaultWriters() {
        return List.of(new TextDocumentWriter(), new WordDocumentWriter());
    }

    public boolean canWrite(DocumentType type) {
        return writers.stream().anyMatch(writer -> writer.supports(type));
    }

    public void write(String content, Path target, DocumentType type) throws IOException {
        Objects.requireNonNull(target, "target");
        for (DocumentWriter writer : writers) {
            if (writer.supports(type)) {
                writer.write(content, target);
                return;
            }
        }
        throw new UnsupportedDocumentException("Editing is not supported for " + type + " documents");
    }

    public void writeWord(Path source, List<WordDocumentFormat.Paragraph> original,
            List<String> edited, Path target) throws IOException {
        for (DocumentWriter writer : writers) {
            if (writer instanceof WordDocumentWriter wordWriter) {
                wordWriter.writePreservingFormat(source, original, edited, target);
                return;
            }
        }
        throw new UnsupportedDocumentException("Editing is not supported for DOCX documents");
    }
}
