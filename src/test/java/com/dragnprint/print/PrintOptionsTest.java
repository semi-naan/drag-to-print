package com.dragnprint.print;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PrintOptionsTest {
    @Test
    void allSelectsEveryPageForEveryCopy() {
        PrintOptions options = PrintOptions.all(5, 2);
        assertEquals(1, options.fromPage());
        assertEquals(5, options.toPage());
        assertEquals(2, options.copies());
        assertEquals(5, options.selectedPageCount());
    }

    @Test
    void parsesAllKeywords() {
        assertEquals(3, PrintOptions.parse("all", 3, 1).selectedPageCount());
        assertEquals(3, PrintOptions.parse("ALL", 3, 1).selectedPageCount());
        assertEquals(3, PrintOptions.parse("*", 3, 1).selectedPageCount());
        assertEquals(3, PrintOptions.parse("", 3, 1).selectedPageCount());
    }

    @Test
    void parsesSinglePageAndRange() {
        PrintOptions single = PrintOptions.parse("2", 5, 1);
        assertEquals(2, single.fromPage());
        assertEquals(2, single.toPage());

        PrintOptions range = PrintOptions.parse("2-4", 5, 3);
        assertEquals(2, range.fromPage());
        assertEquals(4, range.toPage());
        assertEquals(3, range.selectedPageCount());
        assertEquals(9, range.selectedPageCount() * range.copies());
    }

    @Test
    void rejectsInvalidSpecifications() {
        assertThrows(IllegalArgumentException.class, () -> PrintOptions.parse("0", 5, 1));
        assertThrows(IllegalArgumentException.class, () -> PrintOptions.parse("4-2", 5, 1));
        assertThrows(IllegalArgumentException.class, () -> PrintOptions.parse("abc", 5, 1));
        assertThrows(IllegalArgumentException.class, () -> PrintOptions.parse("2,4", 5, 1));
        assertThrows(IllegalArgumentException.class, () -> PrintOptions.parse("9", 5, 1));
        assertThrows(IllegalArgumentException.class, () -> PrintOptions.parse("all", 5, 0));
    }

    @Test
    void parsesCopiesWithSafeFallbacks() {
        assertEquals(1, PrintOptions.parseCopies(null));
        assertEquals(1, PrintOptions.parseCopies(""));
        assertEquals(1, PrintOptions.parseCopies("   "));
        assertEquals(3, PrintOptions.parseCopies("3"));
        assertEquals(2, PrintOptions.parseCopies(" 2 "));
    }

    @Test
    void rejectsInvalidCopies() {
        assertThrows(IllegalArgumentException.class, () -> PrintOptions.parseCopies("0"));
        assertThrows(IllegalArgumentException.class, () -> PrintOptions.parseCopies("-1"));
        assertThrows(IllegalArgumentException.class, () -> PrintOptions.parseCopies("100"));
        assertThrows(IllegalArgumentException.class, () -> PrintOptions.parseCopies("abc"));
    }
}
