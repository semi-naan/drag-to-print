package com.dragnprint.io;

import com.dragnprint.preview.PageProvider;
import com.dragnprint.preview.PageRenderException;
import com.dragnprint.preview.RenderedPage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

public final class LazyPdfPageProvider implements PageProvider, AutoCloseable {
    public static final float DEFAULT_DPI = 150f;
    private static final int NEIGHBOURHOOD = 1;

    private final PDDocument document;
    private final PDFRenderer renderer;
    private final int pageCount;
    private final float dpi;
    private final Map<Integer, RenderedPage> cache = new HashMap<>();

    public LazyPdfPageProvider(PDDocument document, float dpi) {
        this.document = document;
        this.renderer = new PDFRenderer(document);
        this.pageCount = document.getNumberOfPages();
        this.dpi = dpi;
    }

    @Override
    public int pageCount() {
        return pageCount;
    }

    @Override
    public RenderedPage page(int index) {
        if (index < 0 || index >= pageCount) {
            throw new IndexOutOfBoundsException("Page " + (index + 1) + " is out of range (1-" + pageCount + ")");
        }
        RenderedPage cached = cache.get(index);
        if (cached != null) {
            return cached;
        }
        RenderedPage rendered;
        try {
            rendered = RenderedPage.ofImage(renderer.renderImageWithDPI(index, dpi));
        } catch (IOException e) {
            throw new PageRenderException("Could not render PDF page " + (index + 1) + ": " + e.getMessage(), e);
        } catch (OutOfMemoryError e) {
            cache.clear();
            throw new PageRenderException("Out of memory rendering PDF page " + (index + 1)
                    + ". This page is too large to rasterize safely.", e);
        }
        cache.put(index, rendered);
        evictOutside(index);
        return rendered;
    }

    public int cachedPageCount() {
        return cache.size();
    }

    private void evictOutside(int center) {
        cache.keySet().removeIf(key -> Math.abs(key - center) > NEIGHBOURHOOD);
    }

    @Override
    public void close() {
        cache.clear();
        try {
            document.close();
        } catch (IOException ignored) {
        }
    }
}
