package com.dragnprint.preview;

import java.awt.image.BufferedImage;
import java.util.Objects;

public final class RenderedPage {
    private final BufferedImage image;
    private final String text;

    private RenderedPage(BufferedImage image, String text) {
        this.image = image;
        this.text = text;
    }

    public static RenderedPage ofImage(BufferedImage image) {
        Objects.requireNonNull(image, "image");
        return new RenderedPage(image, null);
    }

    public static RenderedPage ofText(String text) {
        Objects.requireNonNull(text, "text");
        return new RenderedPage(null, text);
    }

    public boolean hasImage() {
        return image != null;
    }

    public boolean hasText() {
        return text != null;
    }

    public BufferedImage image() {
        return image;
    }

    public String text() {
        return text;
    }

    public int width() {
        return image == null ? 0 : image.getWidth();
    }

    public int height() {
        return image == null ? 0 : image.getHeight();
    }

    @Override
    public String toString() {
        return hasImage() ? "RenderedPage[image " + width() + "x" + height() + "]" : "RenderedPage[text " + text.length() + " chars]";
    }
}
