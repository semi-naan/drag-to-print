package com.dragnprint.io;

import com.dragnprint.model.DocumentType;
import com.dragnprint.preview.RenderedDocument;
import java.io.IOException;
import java.nio.file.Path;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

public final class PdfDocumentReader implements DocumentReader {
    @Override
    public boolean supports(DocumentType type) {
        return type == DocumentType.PDF;
    }

    @Override
    public RenderedDocument read(Path file) throws IOException {
        PDDocument document = Loader.loadPDF(file.toFile());
        try {
            LazyPdfPageProvider provider = new LazyPdfPageProvider(document, LazyPdfPageProvider.DEFAULT_DPI);
            return new RenderedDocument(file.getFileName().toString(), DocumentType.PDF, provider);
        } catch (RuntimeException e) {
            document.close();
            throw e;
        }
    }
}
