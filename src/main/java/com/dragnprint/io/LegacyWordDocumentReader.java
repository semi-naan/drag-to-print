package com.dragnprint.io;

import com.dragnprint.model.DocumentType;
import com.dragnprint.preview.RenderedDocument;
import com.dragnprint.preview.RenderedPage;
import com.dragnprint.util.TextPaginator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;

public final class LegacyWordDocumentReader implements DocumentReader {
    @Override
    public boolean supports(DocumentType type) {
        return type == DocumentType.DOC;
    }

    @Override
    public RenderedDocument read(Path file) throws IOException {
        String text;
        try (HWPFDocument document = new HWPFDocument(Files.newInputStream(file));
                WordExtractor extractor = new WordExtractor(document)) {
            text = extractor.getText();
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new IOException("Failed to read DOC document: " + file.getFileName(), e);
        }
        List<RenderedPage> pages = TextPaginator.paginate(text);
        return new RenderedDocument(file.getFileName().toString(), DocumentType.DOC, pages);
    }
}
