package com.dragnprint.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dragnprint.model.DocumentType;
import com.dragnprint.preview.RenderedDocument;
import com.dragnprint.util.TextPaginator;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.odftoolkit.odfdom.doc.OdfTextDocument;

class OdtDocumentReaderTest {
    @TempDir
    Path tempDir;

    @Test
    void readsOdtText() throws Exception {
        Path file = tempDir.resolve("notes.odt");
        OdfTextDocument document = OdfTextDocument.newTextDocument();
        document.addText("Hello ODT world");
        document.addText("Second paragraph");
        document.save(file.toFile());
        document.close();

        RenderedDocument rendered = new OdtDocumentReader().read(file);

        assertEquals(DocumentType.ODT, rendered.type());
        String text = TextPaginator.join(rendered.pages());
        assertTrue(text.contains("Hello ODT world"));
        assertTrue(text.contains("Second paragraph"));
    }

    @Test
    void supportsOnlyOdtType() {
        OdtDocumentReader reader = new OdtDocumentReader();
        assertTrue(reader.supports(DocumentType.ODT));
        assertEquals(false, reader.supports(DocumentType.DOCX));
    }
}
