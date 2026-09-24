package com.dragnprint.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dragnprint.model.DocumentType;
import com.dragnprint.preview.RenderedDocument;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PdfDocumentReaderTest {
    @TempDir
    Path tempDir;

    private Path createPdf(Path file, int pages) throws IOException {
        try (PDDocument document = new PDDocument()) {
            for (int i = 0; i < pages; i++) {
                PDPage page = new PDPage();
                document.addPage(page);
                try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                    content.beginText();
                    content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                    content.newLineAtOffset(72, 700);
                    content.showText("Page " + (i + 1));
                    content.endText();
                }
            }
            document.save(file.toFile());
        }
        return file;
    }

    @Test
    void reportsEveryPageAndRendersOnDemand() throws IOException {
        Path file = createPdf(tempDir.resolve("doc.pdf"), 3);

        try (RenderedDocument document = new PdfDocumentReader().read(file)) {
            assertEquals(DocumentType.PDF, document.type());
            assertEquals("doc.pdf", document.title());
            assertEquals(3, document.pageCount());
            assertTrue(document.page(0).hasImage());
            assertTrue(document.page(2).hasImage());
            assertTrue(document.page(2).width() > 0);
            assertTrue(document.page(2).height() > 0);
        }
    }

    @Test
    void rejectsCorruptPdf() throws IOException {
        Path file = tempDir.resolve("broken.pdf");
        Files.writeString(file, "definitely not a pdf", StandardCharsets.UTF_8);

        assertThrows(IOException.class, () -> new PdfDocumentReader().read(file));
    }

    @Test
    void supportsOnlyPdfType() {
        PdfDocumentReader reader = new PdfDocumentReader();
        assertTrue(reader.supports(DocumentType.PDF));
        assertEquals(false, reader.supports(DocumentType.IMAGE));
    }
}
