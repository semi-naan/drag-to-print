package com.dragnprint.io;

import com.dragnprint.model.DocumentType;
import com.dragnprint.preview.RenderedDocument;
import com.dragnprint.preview.RenderedPage;
import com.dragnprint.util.TextPaginator;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.odftoolkit.odfdom.doc.OdfTextDocument;
import org.odftoolkit.odfdom.incubator.doc.text.OdfTextExtractor;

public final class OdtDocumentReader implements DocumentReader {
    @Override
    public boolean supports(DocumentType type) {
        return type == DocumentType.ODT;
    }

    @Override
    public RenderedDocument read(Path file) throws IOException {
        OdfTextDocument document = null;
        try {
            document = OdfTextDocument.loadDocument(file.toFile());
            OdfTextExtractor extractor = OdfTextExtractor.newOdfTextExtractor(document.getContentRoot());
            String text = extractor.getText();
            List<RenderedPage> pages = TextPaginator.paginate(text);
            return new RenderedDocument(file.getFileName().toString(), DocumentType.ODT, pages);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Failed to read ODT document: " + file.getFileName(), e);
        } finally {
            if (document != null) {
                document.close();
            }
        }
    }
}
