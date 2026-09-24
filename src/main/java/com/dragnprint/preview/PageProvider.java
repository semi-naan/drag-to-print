package com.dragnprint.preview;

public interface PageProvider {
    int pageCount();

    RenderedPage page(int index);
}
