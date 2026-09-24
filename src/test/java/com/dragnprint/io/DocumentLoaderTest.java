package com.dragnprint.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dragnprint.model.DocumentModel;
import com.dragnprint.model.DocumentType;
import com.dragnprint.preview.PageProvider;
import com.dragnprint.preview.RenderedDocument;
import com.dragnprint.preview.RenderedPage;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DocumentLoaderTest {
    @TempDir
    Path tempDir;

    private static final class TrackingProvider implements PageProvider, AutoCloseable {
        private final AtomicBoolean closed;

        TrackingProvider(AtomicBoolean closed) {
            this.closed = closed;
        }

        @Override
        public int pageCount() {
            return 1;
        }

        @Override
        public RenderedPage page(int index) {
            return RenderedPage.ofImage(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB));
        }

        @Override
        public void close() {
            closed.set(true);
        }
    }

    private static final class StubReader implements DocumentReader {
        private final AtomicBoolean closed;
        private final boolean outOfMemory;
        private int reads;

        StubReader(AtomicBoolean closed, boolean outOfMemory) {
            this.closed = closed;
            this.outOfMemory = outOfMemory;
        }

        @Override
        public boolean supports(DocumentType type) {
            return type == DocumentType.PDF;
        }

        @Override
        public RenderedDocument read(Path file) throws IOException {
            reads++;
            if (outOfMemory) {
                throw new OutOfMemoryError("simulated");
            }
            return new RenderedDocument(file.getFileName().toString(), DocumentType.PDF,
                    new TrackingProvider(closed));
        }
    }

    private DocumentLoader loader(DocumentReader reader) {
        return new DocumentLoader(new DocumentReaders(List.of(reader)));
    }

    @Test
    void closesDocumentsProducedBeforeCancellation() throws IOException {
        Path a = Files.writeString(tempDir.resolve("a.pdf"), "a");
        Path b = Files.writeString(tempDir.resolve("b.pdf"), "b");
        AtomicBoolean closed = new AtomicBoolean();
        StubReader reader = new StubReader(closed, false);
        AtomicInteger checks = new AtomicInteger();
        BooleanSupplier cancelled = () -> checks.getAndIncrement() >= 1;

        DocumentLoader.Result result = loader(reader).load(List.of(a.toFile(), b.toFile()), cancelled);

        assertTrue(result.cancelled());
        assertTrue(result.documents().isEmpty());
        assertTrue(closed.get());
        assertEquals(1, reader.reads);
    }

    @Test
    void transfersDocumentsWhenNotCancelled() throws IOException {
        Path a = Files.writeString(tempDir.resolve("a.pdf"), "a");
        Path b = Files.writeString(tempDir.resolve("b.pdf"), "b");
        AtomicBoolean closed = new AtomicBoolean();
        StubReader reader = new StubReader(closed, false);

        DocumentLoader.Result result = loader(reader).load(List.of(a.toFile(), b.toFile()), () -> false);

        assertFalse(result.cancelled());
        assertEquals(2, result.documents().size());
        assertFalse(closed.get());
        result.documents().forEach(DocumentModel::close);
        assertTrue(closed.get());
    }

    @Test
    void stopsAfterOutOfMemoryAndReportsIt() throws IOException {
        Path a = Files.writeString(tempDir.resolve("a.pdf"), "a");
        Path b = Files.writeString(tempDir.resolve("b.pdf"), "b");
        StubReader reader = new StubReader(new AtomicBoolean(), true);

        DocumentLoader.Result result = loader(reader).load(List.of(a.toFile(), b.toFile()), () -> false);

        assertFalse(result.cancelled());
        assertTrue(result.documents().isEmpty());
        assertEquals(1, reader.reads);
        assertTrue(result.notices().stream().anyMatch(notice -> notice.contains("out of memory")));
    }

    @Test
    void reportsUnsupportedAndDirectoryEntries() throws IOException {
        Path supported = Files.writeString(tempDir.resolve("a.pdf"), "a");
        Path unsupported = Files.writeString(tempDir.resolve("a.zip"), "z");
        StubReader reader = new StubReader(new AtomicBoolean(), false);

        DocumentLoader.Result result = loader(reader).load(
                List.of(unsupported.toFile(), tempDir.toFile(), supported.toFile()), () -> false);

        assertEquals(1, result.documents().size());
        assertEquals(2, result.notices().size());
        result.documents().forEach(DocumentModel::close);
    }
}
