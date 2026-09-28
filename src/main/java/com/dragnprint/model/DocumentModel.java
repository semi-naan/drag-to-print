package com.dragnprint.model;

import com.dragnprint.io.WordDocumentFormat;
import com.dragnprint.preview.RenderedDocument;
import com.dragnprint.preview.RenderedPage;
import com.dragnprint.util.TextPaginator;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class DocumentModel implements AutoCloseable {
    private Path path;
    private final DocumentType type;
    private RenderedDocument rendered;
    private final List<WordDocumentFormat.Paragraph> wordParagraphs;
    private final List<String> editedParagraphs;
    private String text;
    private String savedText;
    private boolean dirty;
    private boolean closed;

    public DocumentModel(Path path, DocumentType type, RenderedDocument rendered) {
        this(path, type, rendered, null);
    }

    public DocumentModel(Path path, DocumentType type, RenderedDocument rendered,
            List<WordDocumentFormat.Paragraph> wordParagraphs) {
        this.path = Objects.requireNonNull(path, "path");
        this.type = Objects.requireNonNull(type, "type");
        this.rendered = Objects.requireNonNull(rendered, "rendered");
        this.wordParagraphs = wordParagraphs == null ? null : new ArrayList<>(wordParagraphs);
        this.editedParagraphs = this.wordParagraphs == null ? null : new ArrayList<>(this.wordParagraphs.stream()
                .map(WordDocumentFormat.Paragraph::text).toList());
        this.text = type == DocumentType.TEXT ? TextPaginator.join(rendered.pages()) : null;
        this.savedText = this.text;
        this.dirty = false;
    }

    public Path path() {
        return path;
    }

    public void setPath(Path path) {
        this.path = Objects.requireNonNull(path, "path");
    }

    public DocumentType type() {
        return type;
    }

    public boolean isEditable() {
        return type.isEditable() && (text != null || editedParagraphs != null);
    }

    public String text() {
        return text;
    }

    public void setText(String text) {
        if (this.text != null) {
            this.text = Objects.requireNonNull(text, "text");
            this.dirty = !Objects.equals(savedText, text);
        }
    }

    public boolean isDirty() {
        return dirty;
    }

    public void markSaved() {
        if (editedParagraphs != null) {
            for (int i = 0; i < editedParagraphs.size(); i++) {
                WordDocumentFormat.Paragraph paragraph = wordParagraphs.get(i);
                wordParagraphs.set(i, new WordDocumentFormat.Paragraph(editedParagraphs.get(i), paragraph.editable()));
            }
        }
        savedText = text;
        dirty = false;
    }

    public List<WordDocumentFormat.Paragraph> wordParagraphs() {
        return wordParagraphs == null ? List.of() : List.copyOf(wordParagraphs);
    }

    public List<String> editedParagraphs() {
        return editedParagraphs == null ? List.of() : List.copyOf(editedParagraphs);
    }

    public void setParagraphText(int index, String value) {
        if (editedParagraphs == null || !wordParagraphs.get(index).editable()) {
            throw new IllegalArgumentException("This paragraph is not editable.");
        }
        editedParagraphs.set(index, Objects.requireNonNull(value, "value"));
        dirty = false;
        for (int i = 0; i < editedParagraphs.size(); i++) {
            if (!editedParagraphs.get(i).equals(wordParagraphs.get(i).text())) {
                dirty = true;
                break;
            }
        }
    }

    public void replaceRendered(RenderedDocument replacement) {
        rendered.close();
        rendered = Objects.requireNonNull(replacement, "replacement");
    }

    public RenderedDocument toRendered() {
        if (text == null) {
            return rendered;
        }
        List<RenderedPage> pages = TextPaginator.paginate(text);
        return new RenderedDocument(path.getFileName().toString(), type, pages);
    }

    public String displayName() {
        String name = path.getFileName() == null ? path.toString() : path.getFileName().toString();
        return dirty ? name + " *" : name;
    }

    @Override
    public String toString() {
        return displayName();
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        rendered.close();
    }
}
