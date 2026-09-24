package com.dragnprint.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dragnprint.model.DocumentType;
import com.dragnprint.preview.RenderedDocument;
import com.dragnprint.util.TextPaginator;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DocumentReadersTest {
    @TempDir
    Path tempDir;

    @Test
    void dispatchesToCorrectReaderByExtension() throws IOException {
        Path file = tempDir.resolve("notes.txt");
        Files.writeString(file, "content", StandardCharsets.UTF_8);

        RenderedDocument document = new DocumentReaders().read(file);

        assertEquals(DocumentType.TEXT, document.type());
        assertEquals("content", TextPaginator.join(document.pages()));
    }

    @Test
    void rejectsUnsupportedFiles() throws IOException {
        Path file = tempDir.resolve("archive.zip");
        Files.writeString(file, "data", StandardCharsets.UTF_8);

        UnsupportedDocumentException exception = assertThrows(UnsupportedDocumentException.class,
                () -> new DocumentReaders().read(file));
        assertTrue(exception.getMessage().contains("archive.zip"));
    }

    @Test
    void reportsMissingFileAsIoError() {
        Path file = tempDir.resolve("missing.txt");
        assertThrows(IOException.class, () -> new DocumentReaders().read(file));
    }
}
