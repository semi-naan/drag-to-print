package com.dragnprint.preview;

import com.dragnprint.model.DocumentType;
import java.util.List;
import java.util.Objects;

public final class RenderedDocument implements AutoCloseable {
    private final String title;
    private final DocumentType type;
    private final PageProvider provider;
    private boolean closed;

    public RenderedDocument(String title, DocumentType type, List<RenderedPage> pages) {
        this(title, type, new ListPageProvider(pages));
    }

    public RenderedDocument(String title, DocumentType type, PageProvider provider) {
        this.title = Objects.requireNonNull(title, "title");
        this.type = Objects.requireNonNull(type, "type");
        this.provider = Objects.requireNonNull(provider, "provider");
    }

    public String title() {
        return title;
    }

    public DocumentType type() {
        return type;
    }

    public List<RenderedPage> pages() {
        if (provider instanceof ListPageProvider list) {
            return list.pages();
        }
        throw new UnsupportedOperationException(
                "This document renders pages lazily; use pageCount() with page(index) instead of pages()");
    }

    public int pageCount() {
        return provider.pageCount();
    }

    public boolean isEmpty() {
        return provider.pageCount() == 0;
    }

    public RenderedPage page(int index) {
        return provider.page(index);
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        if (provider instanceof AutoCloseable closeable) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public String toString() {
        return "RenderedDocument[" + title + ", " + type + ", " + pageCount() + " page(s)]";
    }
}
