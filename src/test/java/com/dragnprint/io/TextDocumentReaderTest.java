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
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TextDocumentReaderTest {
    @TempDir
    Path tempDir;

    @Test
    void readsUtf8Text() throws IOException {
        Path file = tempDir.resolve("notes.txt");
        Files.writeString(file, "caf\u00e9\nsecond line", StandardCharsets.UTF_8);

        RenderedDocument document = new TextDocumentReader().read(file);

        assertEquals(DocumentType.TEXT, document.type());
        assertEquals("notes.txt", document.title());
        assertEquals("caf\u00e9\nsecond line", TextPaginator.join(document.pages()));
    }

    @Test
    void paginatesLongFiles() throws IOException {
        Path file = tempDir.resolve("long.md");
        String text = IntStream.rangeClosed(1, 120)
                .mapToObj(i -> "line " + i)
                .collect(Collectors.joining("\n"));
        Files.writeString(file, text, StandardCharsets.UTF_8);

        RenderedDocument document = new TextDocumentReader().read(file);

        assertTrue(document.pageCount() > 1);
        assertEquals(text, TextPaginator.join(document.pages()));
    }

    @Test
    void supportsOnlyTextType() {
        TextDocumentReader reader = new TextDocumentReader();
        assertTrue(reader.supports(DocumentType.TEXT));
        assertEquals(false, reader.supports(DocumentType.PDF));
    }

    @Test
    void failsForMissingFile() {
        assertThrows(IOException.class, () -> new TextDocumentReader().read(tempDir.resolve("missing.txt")));
    }
}
