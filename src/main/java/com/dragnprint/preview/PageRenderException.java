package com.dragnprint.preview;

public final class PageRenderException extends RuntimeException {
    public PageRenderException(String message) {
        super(message);
    }

    public PageRenderException(String message, Throwable cause) {
        super(message, cause);
    }
}
