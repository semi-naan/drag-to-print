package com.dragnprint.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class DocumentTypeTest {
    @Test
    void classifiesPdfCaseInsensitively() {
        assertEquals(DocumentType.PDF, DocumentType.classify("Report.PDF"));
        assertEquals(DocumentType.PDF, DocumentType.classify("a/b/c/notes.pdf"));
    }

    @Test
    void classifiesImages() {
        assertEquals(DocumentType.IMAGE, DocumentType.classify("photo.PNG"));
        assertEquals(DocumentType.IMAGE, DocumentType.classify("photo.JPeG"));
        assertEquals(DocumentType.IMAGE, DocumentType.classify("scan.tiff"));
        assertEquals(DocumentType.IMAGE, DocumentType.classify("art.BMP"));
        assertEquals(DocumentType.IMAGE, DocumentType.classify("anim.gif"));
    }

    @Test
    void classifiesEditableDocuments() {
        assertEquals(DocumentType.DOCX, DocumentType.classify("letter.docx"));
        assertEquals(DocumentType.TEXT, DocumentType.classify("notes.txt"));
        assertEquals(DocumentType.TEXT, DocumentType.classify("readme.MD"));
        assertEquals(DocumentType.TEXT, DocumentType.classify("guide.markdown"));
    }

    @Test
    void classifiesReadOnlyDocuments() {
        assertEquals(DocumentType.DOC, DocumentType.classify("old.DOC"));
        assertEquals(DocumentType.ODT, DocumentType.classify("open.odt"));
    }

    @Test
    void classifiesUnknownAndMissingExtensionsAsUnsupported() {
        assertEquals(DocumentType.UNSUPPORTED, DocumentType.classify("archive.zip"));
        assertEquals(DocumentType.UNSUPPORTED, DocumentType.classify("noextension"));
        assertEquals(DocumentType.UNSUPPORTED, DocumentType.classify("trailing."));
        assertEquals(DocumentType.UNSUPPORTED, DocumentType.classify((String) null));
        assertEquals(DocumentType.UNSUPPORTED, DocumentType.classify((Path) null));
    }

    @Test
    void fallsBackToMimeTypeWhenExtensionUnknown() {
        assertEquals(DocumentType.PDF, DocumentType.classify("blob", "application/pdf"));
        assertEquals(DocumentType.IMAGE, DocumentType.classify("blob", "image/png"));
        assertEquals(DocumentType.DOCX, DocumentType.classify("blob",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
        assertEquals(DocumentType.DOC, DocumentType.classify("blob", "application/msword"));
        assertEquals(DocumentType.ODT, DocumentType.classify("blob", "application/vnd.oasis.opendocument.text"));
        assertEquals(DocumentType.TEXT, DocumentType.classify("blob", "text/plain; charset=utf-8"));
        assertEquals(DocumentType.UNSUPPORTED, DocumentType.classify("blob", "application/octet-stream"));
    }

    @Test
    void extensionTakesPrecedenceOverMimeType() {
        assertEquals(DocumentType.TEXT, DocumentType.classify("notes.txt", "application/pdf"));
    }

    @Test
    void exposesEditableAndSupportedFlags() {
        assertTrue(DocumentType.DOCX.isEditable());
        assertTrue(DocumentType.TEXT.isEditable());
        assertFalse(DocumentType.PDF.isEditable());
        assertFalse(DocumentType.IMAGE.isEditable());
        assertFalse(DocumentType.DOC.isEditable());
        assertFalse(DocumentType.ODT.isEditable());

        assertTrue(DocumentType.PDF.isSupported());
        assertTrue(DocumentType.DOC.isSupported());
        assertTrue(DocumentType.ODT.isSupported());
        assertFalse(DocumentType.UNSUPPORTED.isSupported());
    }
}
