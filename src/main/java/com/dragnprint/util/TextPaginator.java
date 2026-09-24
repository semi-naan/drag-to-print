package com.dragnprint.util;

import com.dragnprint.preview.RenderedPage;
import java.util.ArrayList;
import java.util.List;

public final class TextPaginator {
    public static final int DEFAULT_LINES_PER_PAGE = 48;

    private TextPaginator() {
    }

    public static List<RenderedPage> paginate(String text) {
        return paginate(text, DEFAULT_LINES_PER_PAGE);
    }

    public static List<RenderedPage> paginate(String text, int linesPerPage) {
        if (linesPerPage < 1) {
            throw new IllegalArgumentException("linesPerPage must be >= 1");
        }
        String normalized = text == null ? "" : text.replace("\r\n", "\n").replace('\r', '\n');
        String[] lines = normalized.split("\n", -1);
        List<RenderedPage> pages = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int count = 0;
        for (String line : lines) {
            if (count == linesPerPage) {
                pages.add(RenderedPage.ofText(current.toString()));
                current.setLength(0);
                count = 0;
            }
            if (count > 0) {
                current.append('\n');
            }
            current.append(line);
            count++;
        }
        if (current.length() > 0 || pages.isEmpty()) {
            pages.add(RenderedPage.ofText(current.toString()));
        }
        return List.copyOf(pages);
    }

    public static String join(List<RenderedPage> pages) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (RenderedPage page : pages) {
            if (!page.hasText()) {
                continue;
            }
            if (!first) {
                builder.append('\n');
            }
            builder.append(page.text());
            first = false;
        }
        return builder.toString();
    }
}
