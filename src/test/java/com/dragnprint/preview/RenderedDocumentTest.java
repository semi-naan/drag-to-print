package com.dragnprint.preview;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dragnprint.model.DocumentType;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class RenderedDocumentTest {
    @Test
    void exposesMetadataAndPages() {
        RenderedPage text = RenderedPage.ofText("hello");
        RenderedDocument document = new RenderedDocument("hello.txt", DocumentType.TEXT, List.of(text));

        assertEquals("hello.txt", document.title());
        assertEquals(DocumentType.TEXT, document.type());
        assertEquals(1, document.pageCount());
        assertEquals(text, document.page(0));
        assertFalse(document.isEmpty());
    }

    @Test
    void supportsEmptyDocuments() {
        RenderedDocument document = new RenderedDocument("empty.txt", DocumentType.TEXT, List.of());
        assertTrue(document.isEmpty());
        assertEquals(0, document.pageCount());
    }

    @Test
    void pagesAreImmutable() {
        RenderedDocument document = new RenderedDocument("a.txt", DocumentType.TEXT,
                List.of(RenderedPage.ofText("a")));
        assertThrows(UnsupportedOperationException.class, () -> document.pages().add(RenderedPage.ofText("b")));
    }

    @Test
    void rejectsNullArguments() {
        assertThrows(NullPointerException.class, () -> new RenderedDocument(null, DocumentType.TEXT, List.of()));
        assertThrows(NullPointerException.class, () -> new RenderedDocument("a", null, List.of()));
        assertThrows(NullPointerException.class,
                () -> new RenderedDocument("a", DocumentType.TEXT, (List<RenderedPage>) null));
    }

    @Test
    void supportsLazyPageProviders() {
        int[] calls = {0};
        PageProvider provider = new PageProvider() {
            @Override
            public int pageCount() {
                return 3;
            }

            @Override
            public RenderedPage page(int index) {
                calls[0]++;
                return RenderedPage.ofText("p" + index);
            }
        };
        RenderedDocument document = new RenderedDocument("lazy.pdf", DocumentType.PDF, provider);

        assertEquals(3, document.pageCount());
        assertEquals(0, calls[0]);
        assertEquals("p1", document.page(1).text());
        assertEquals(1, calls[0]);
        assertThrows(UnsupportedOperationException.class, document::pages);
    }

    @Test
    void closesAutoCloseableProviders() {
        class ClosableProvider implements PageProvider, AutoCloseable {
            private final AtomicBoolean closed = new AtomicBoolean();
            private final java.util.concurrent.atomic.AtomicInteger closeCount =
                    new java.util.concurrent.atomic.AtomicInteger();

            @Override
            public int pageCount() {
                return 1;
            }

            @Override
            public RenderedPage page(int index) {
                return RenderedPage.ofText("x");
            }

            @Override
            public void close() {
                closeCount.incrementAndGet();
                closed.set(true);
            }
        }
        ClosableProvider provider = new ClosableProvider();
        RenderedDocument document = new RenderedDocument("lazy.pdf", DocumentType.PDF, provider);

        document.close();
        document.close();

        assertTrue(provider.closed.get());
        assertEquals(1, provider.closeCount.get());
    }

    @Test
    void textPageReportsText() {
        RenderedPage page = RenderedPage.ofText("body");
        assertTrue(page.hasText());
        assertFalse(page.hasImage());
        assertEquals("body", page.text());
        assertEquals(0, page.width());
        assertEquals(0, page.height());
    }

    @Test
    void imagePageReportsDimensions() {
        BufferedImage image = new BufferedImage(12, 8, BufferedImage.TYPE_INT_RGB);
        RenderedPage page = RenderedPage.ofImage(image);
        assertTrue(page.hasImage());
        assertFalse(page.hasText());
        assertEquals(12, page.width());
        assertEquals(8, page.height());
    }
}
