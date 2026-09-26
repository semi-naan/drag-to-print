package com.dragnprint.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TextStatisticsTest {
    @Test
    void countsWordsSeparatedByWhitespace() {
        assertEquals(2, TextStatistics.wordCount("hello world"));
    }

    @Test
    void countsWordsAcrossWhitespaceRuns() {
        assertEquals(3, TextStatistics.wordCount("  one\ttwo\n\nthree  "));
    }

    @Test
    void countsZeroWordsForEmptyOrBlankText() {
        assertEquals(0, TextStatistics.wordCount(""));
        assertEquals(0, TextStatistics.wordCount("   \n\t "));
        assertEquals(0, TextStatistics.wordCount(null));
    }

    @Test
    void countsCharactersIncludingWhitespace() {
        assertEquals(0, TextStatistics.characterCount(""));
        assertEquals(3, TextStatistics.characterCount("abc"));
        assertEquals(3, TextStatistics.characterCount("a\nb"));
        assertEquals(0, TextStatistics.characterCount(null));
    }
}
