package com.dragnprint.io;

import com.dragnprint.model.DocumentType;
import com.dragnprint.preview.RenderedDocument;
import com.dragnprint.preview.RenderedPage;
import com.dragnprint.util.TextPaginator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

public final class WordDocumentReader implements DocumentReader {
    @Override
    public boolean supports(DocumentType type) {
        return type == DocumentType.DOCX;
    }

    @Override
    public RenderedDocument read(Path file) throws IOException {
        String text;
        try (XWPFDocument document = new XWPFDocument(Files.newInputStream(file));
                XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            text = extractor.getText();
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new IOException("Failed to read DOCX document: " + file.getFileName(), e);
        }
        List<RenderedPage> pages = TextPaginator.paginate(text);
        return new RenderedDocument(file.getFileName().toString(), DocumentType.DOCX, pages);
    }
}
