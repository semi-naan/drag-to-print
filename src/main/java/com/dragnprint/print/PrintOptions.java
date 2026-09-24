package com.dragnprint.print;

import java.util.Locale;

public final class PrintOptions {
    private final int fromPage;
    private final int toPage;
    private final int copies;

    private PrintOptions(int fromPage, int toPage, int copies) {
        this.fromPage = fromPage;
        this.toPage = toPage;
        this.copies = copies;
    }

    public static PrintOptions all(int pageCount, int copies) {
        if (pageCount < 0) {
            throw new IllegalArgumentException("pageCount must be >= 0");
        }
        if (copies < 1) {
            throw new IllegalArgumentException("copies must be >= 1");
        }
        return new PrintOptions(1, pageCount, copies);
    }

    public static PrintOptions ofRange(int fromPage, int toPage, int copies) {
        if (fromPage < 1) {
            throw new IllegalArgumentException("fromPage must be >= 1");
        }
        if (toPage < fromPage) {
            throw new IllegalArgumentException("toPage must be >= fromPage");
        }
        if (copies < 1) {
            throw new IllegalArgumentException("copies must be >= 1");
        }
        return new PrintOptions(fromPage, toPage, copies);
    }

    public static PrintOptions parse(String spec, int pageCount, int copies) {
        if (pageCount < 0) {
            throw new IllegalArgumentException("pageCount must be >= 0");
        }
        if (copies < 1) {
            throw new IllegalArgumentException("copies must be >= 1");
        }
        String value = spec == null ? "" : spec.trim().toLowerCase(Locale.ROOT);
        PrintOptions options;
        if (value.isEmpty() || value.equals("all") || value.equals("*")) {
            options = all(pageCount, copies);
        } else if (value.contains(",")) {
            throw new IllegalArgumentException("Only a single page or range is supported");
        } else if (value.contains("-")) {
            String[] parts = value.split("-", -1);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid page range: " + spec);
            }
            options = ofRange(parsePageNumber(parts[0]), parsePageNumber(parts[1]), copies);
        } else {
            int page = parsePageNumber(value);
            options = ofRange(page, page, copies);
        }
        if (pageCount > 0 && options.toPage > pageCount) {
            throw new IllegalArgumentException("Page " + options.toPage + " is out of range (1-" + pageCount + ")");
        }
        return options;
    }

    public static int parseCopies(String text) {
        if (text == null || text.isBlank()) {
            return 1;
        }
        int value;
        try {
            value = Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Copies must be a whole number");
        }
        if (value < 1) {
            throw new IllegalArgumentException("Copies must be at least 1");
        }
        if (value > 99) {
            throw new IllegalArgumentException("Copies must be at most 99");
        }
        return value;
    }

    private static int parsePageNumber(String value) {
        try {
            int page = Integer.parseInt(value.trim());
            if (page < 1) {
                throw new IllegalArgumentException("Page numbers start at 1");
            }
            return page;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid page number: " + value.trim());
        }
    }

    public int fromPage() {
        return fromPage;
    }

    public int toPage() {
        return toPage;
    }

    public int copies() {
        return copies;
    }

    public int selectedPageCount() {
        return Math.max(0, toPage - fromPage + 1);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PrintOptions that)) {
            return false;
        }
        return fromPage == that.fromPage && toPage == that.toPage && copies == that.copies;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(fromPage, toPage, copies);
    }

    @Override
    public String toString() {
        return "PrintOptions[pages " + fromPage + "-" + toPage + ", copies " + copies + "]";
    }
}
