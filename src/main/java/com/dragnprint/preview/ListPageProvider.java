package com.dragnprint.preview;

import java.util.List;
import java.util.Objects;

final class ListPageProvider implements PageProvider {
    private final List<RenderedPage> pages;

    ListPageProvider(List<RenderedPage> pages) {
        this.pages = List.copyOf(Objects.requireNonNull(pages, "pages"));
    }

    List<RenderedPage> pages() {
        return pages;
    }

    @Override
    public int pageCount() {
        return pages.size();
    }

    @Override
    public RenderedPage page(int index) {
        return pages.get(index);
    }
}
