package com.dragnprint.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dragnprint.model.DocumentType;
import com.dragnprint.preview.RenderedDocument;
import com.dragnprint.util.TextPaginator;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class WordDocumentReaderTest {
    @TempDir
    Path tempDir;

    private Path createDocx(Path file, String... paragraphs) throws IOException {
        try (XWPFDocument document = new XWPFDocument();
                OutputStream output = Files.newOutputStream(file)) {
            for (String line : paragraphs) {
                XWPFParagraph paragraph = document.createParagraph();
                XWPFRun run = paragraph.createRun();
                run.setText(line);
            }
            document.write(output);
        }
        return file;
    }

    @Test
    void readsDocxText() throws IOException {
        Path file = createDocx(tempDir.resolve("letter.docx"), "Hello DOCX world", "Second paragraph");

        RenderedDocument document = new WordDocumentReader().read(file);

        assertEquals(DocumentType.DOCX, document.type());
        String text = TextPaginator.join(document.pages());
        assertTrue(text.contains("Hello DOCX world"));
        assertTrue(text.contains("Second paragraph"));
    }

    @Test
    void rejectsCorruptDocx() throws IOException {
        Path file = tempDir.resolve("broken.docx");
        Files.writeString(file, "not a docx", StandardCharsets.UTF_8);

        assertThrows(IOException.class, () -> new WordDocumentReader().read(file));
    }

    @Test
    void supportsOnlyDocxType() {
        WordDocumentReader reader = new WordDocumentReader();
        assertTrue(reader.supports(DocumentType.DOCX));
        assertEquals(false, reader.supports(DocumentType.DOC));
    }
}
