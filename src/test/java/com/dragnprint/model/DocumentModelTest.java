package com.dragnprint.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dragnprint.preview.RenderedDocument;
import com.dragnprint.preview.RenderedPage;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class DocumentModelTest {
    @Test
    void editableTextDocumentExposesJoinedText() {
        RenderedDocument rendered = new RenderedDocument("notes.txt", DocumentType.TEXT,
                List.of(RenderedPage.ofText("first"), RenderedPage.ofText("second")));
        DocumentModel model = new DocumentModel(Path.of("/tmp/notes.txt"), DocumentType.TEXT, rendered);

        assertTrue(model.isEditable());
        assertEquals("first\nsecond", model.text());
        assertFalse(model.isDirty());
        assertEquals("notes.txt", model.displayName());
    }

    @Test
    void dirtyFlagDecoratesDisplayName() {
        RenderedDocument rendered = new RenderedDocument("notes.txt", DocumentType.TEXT,
                List.of(RenderedPage.ofText("hi")));
        DocumentModel model = new DocumentModel(Path.of("/tmp/notes.txt"), DocumentType.TEXT, rendered);
        model.setDirty(true);
        assertEquals("notes.txt *", model.displayName());
    }

    @Test
    void toRenderedReflectsEditedText() {
        RenderedDocument rendered = new RenderedDocument("notes.txt", DocumentType.TEXT,
                List.of(RenderedPage.ofText("before")));
        DocumentModel model = new DocumentModel(Path.of("/tmp/notes.txt"), DocumentType.TEXT, rendered);
        model.setText("after");
        assertEquals("after", model.toRendered().page(0).text());
    }

    @Test
    void nonEditableDocumentHasNoText() {
        RenderedDocument rendered = new RenderedDocument("scan.png", DocumentType.IMAGE,
                List.of(RenderedPage.ofImage(new java.awt.image.BufferedImage(2, 2, java.awt.image.BufferedImage.TYPE_INT_RGB))));
        DocumentModel model = new DocumentModel(Path.of("/tmp/scan.png"), DocumentType.IMAGE, rendered);

        assertFalse(model.isEditable());
        assertEquals(null, model.text());
        assertEquals(rendered, model.toRendered());
    }
}
