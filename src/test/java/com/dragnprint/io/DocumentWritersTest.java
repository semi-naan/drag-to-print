package com.dragnprint.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dragnprint.model.DocumentType;
import com.dragnprint.util.TextPaginator;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DocumentWritersTest {
    @TempDir
    Path tempDir;

    @Test
    void reportsWhichTypesAreWritable() {
        DocumentWriters writers = new DocumentWriters();
        assertTrue(writers.canWrite(DocumentType.TEXT));
        assertTrue(writers.canWrite(DocumentType.DOCX));
        assertFalse(writers.canWrite(DocumentType.PDF));
        assertFalse(writers.canWrite(DocumentType.IMAGE));
    }

    @Test
    void writesUtf8Text() throws IOException {
        Path file = tempDir.resolve("out.txt");
        new DocumentWriters().write("caf\u00e9\nline", file, DocumentType.TEXT);
        assertEquals("caf\u00e9\nline", Files.readString(file, StandardCharsets.UTF_8));
    }

    @Test
    void writesDocxThatCanBeReadBack() throws IOException {
        Path file = tempDir.resolve("out.docx");
        DocumentWriters writers = new DocumentWriters();
        writers.write("first line\nsecond line", file, DocumentType.DOCX);

        var rendered = new WordDocumentReader().read(file);
        String text = TextPaginator.join(rendered.pages());
        assertTrue(text.contains("first line"));
        assertTrue(text.contains("second line"));
    }

    @Test
    void rejectsReadOnlyTypes() {
        DocumentWriters writers = new DocumentWriters();
        UnsupportedDocumentException exception = assertThrows(UnsupportedDocumentException.class,
                () -> writers.write("x", tempDir.resolve("out.pdf"), DocumentType.PDF));
        assertTrue(exception.getMessage().contains("not supported"));
    }
}
