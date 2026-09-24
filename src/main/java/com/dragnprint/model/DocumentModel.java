package com.dragnprint.model;

import com.dragnprint.preview.RenderedDocument;
import com.dragnprint.preview.RenderedPage;
import com.dragnprint.util.TextPaginator;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public final class DocumentModel implements AutoCloseable {
    private Path path;
    private final DocumentType type;
    private final RenderedDocument rendered;
    private String text;
    private boolean dirty;
    private boolean closed;

    public DocumentModel(Path path, DocumentType type, RenderedDocument rendered) {
        this.path = Objects.requireNonNull(path, "path");
        this.type = Objects.requireNonNull(type, "type");
        this.rendered = Objects.requireNonNull(rendered, "rendered");
        this.text = type.isEditable() ? TextPaginator.join(rendered.pages()) : null;
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
        return type.isEditable() && text != null;
    }

    public String text() {
        return text;
    }

    public void setText(String text) {
        if (isEditable()) {
            this.text = Objects.requireNonNull(text, "text");
        }
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public RenderedDocument toRendered() {
        if (!isEditable()) {
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
