package com.dragnprint.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dragnprint.preview.RenderedDocument;
import com.dragnprint.preview.RenderedPage;
import com.dragnprint.io.WordDocumentFormat;
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
        model.setText("hi there");
        assertEquals("notes.txt *", model.displayName());
    }

    @Test
    void editingMarksDirtyAndRevertingToSavedTextClearsIt() {
        DocumentModel model = editableDocument("notes.txt", "hello");
        assertFalse(model.isDirty());

        model.setText("hello world");
        assertTrue(model.isDirty());

        model.setText("hello");
        assertFalse(model.isDirty());
    }

    @Test
    void markSavedMovesBaselineToCurrentText() {
        DocumentModel model = editableDocument("notes.txt", "hello");
        model.setText("hello world");
        assertTrue(model.isDirty());

        model.markSaved();
        assertFalse(model.isDirty());

        model.setText("hello");
        assertTrue(model.isDirty());

        model.setText("hello world");
        assertFalse(model.isDirty());
    }

    private static DocumentModel editableDocument(String name, String text) {
        RenderedDocument rendered = new RenderedDocument(name, DocumentType.TEXT,
                List.of(RenderedPage.ofText(text)));
        return new DocumentModel(Path.of("/tmp/" + name), DocumentType.TEXT, rendered);
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

    @Test
    void wordDocumentKeepsOriginalPreviewAndTracksParagraphEdits() {
        RenderedDocument rendered = new RenderedDocument("letter.docx", DocumentType.DOCX,
                List.of(RenderedPage.ofText("Hello world")));
        DocumentModel model = new DocumentModel(Path.of("letter.docx"), DocumentType.DOCX, rendered,
                List.of(new WordDocumentFormat.Paragraph("Hello world", true)));

        assertTrue(model.isEditable());
        assertEquals(rendered, model.toRendered());
        model.setParagraphText(0, "Hello friend");
        assertTrue(model.isDirty());
        assertEquals(rendered, model.toRendered());
        model.markSaved();
        assertFalse(model.isDirty());
        assertEquals("Hello friend", model.wordParagraphs().getFirst().text());
    }
}
