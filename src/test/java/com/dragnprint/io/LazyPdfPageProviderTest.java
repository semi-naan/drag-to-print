package com.dragnprint.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LazyPdfPageProviderTest {
    @TempDir
    Path tempDir;

    private Path createPdf(int pages) throws IOException {
        Path file = tempDir.resolve("lazy.pdf");
        try (PDDocument document = new PDDocument()) {
            for (int i = 0; i < pages; i++) {
                document.addPage(new PDPage());
            }
            document.save(file.toFile());
        }
        return file;
    }

    @Test
    void rendersLazilyAndBoundsTheCache() throws IOException {
        PDDocument pdf = Loader.loadPDF(createPdf(6).toFile());
        LazyPdfPageProvider provider = new LazyPdfPageProvider(pdf, 36f);
        try {
            assertEquals(6, provider.pageCount());
            assertEquals(0, provider.cachedPageCount());
            assertTrue(provider.page(0).hasImage());
            assertEquals(1, provider.cachedPageCount());
            provider.page(2);
            provider.page(4);
            assertTrue(provider.cachedPageCount() <= 3);
        } finally {
            provider.close();
        }
    }

    @Test
    void rejectsOutOfRangePages() throws IOException {
        PDDocument pdf = Loader.loadPDF(createPdf(2).toFile());
        LazyPdfPageProvider provider = new LazyPdfPageProvider(pdf, 36f);
        try {
            assertThrows(IndexOutOfBoundsException.class, () -> provider.page(-1));
            assertThrows(IndexOutOfBoundsException.class, () -> provider.page(2));
        } finally {
            provider.close();
        }
    }
}
