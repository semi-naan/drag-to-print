package com.dragnprint.util;

public final class TextStatistics {
    private TextStatistics() {
    }

    public static int wordCount(String text) {
        if (text == null) {
            return 0;
        }
        int words = 0;
        boolean inWord = false;
        for (int i = 0; i < text.length(); i++) {
            if (Character.isWhitespace(text.charAt(i))) {
                inWord = false;
            } else if (!inWord) {
                inWord = true;
                words++;
            }
        }
        return words;
    }

    public static int characterCount(String text) {
        return text == null ? 0 : text.length();
    }
}
