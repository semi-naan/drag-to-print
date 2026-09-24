package com.dragnprint.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dragnprint.preview.RenderedPage;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class TextPaginatorTest {
    @Test
    void paginatesLongTextAtLineBoundaries() {
        String text = IntStream.rangeClosed(1, 100)
                .mapToObj(i -> "line " + i)
                .collect(Collectors.joining("\n"));
        List<RenderedPage> pages = TextPaginator.paginate(text, 48);
        assertEquals(3, pages.size());
        assertEquals(48, pages.get(0).text().lines().count());
        assertEquals(48, pages.get(1).text().lines().count());
        assertEquals(4, pages.get(2).text().lines().count());
    }

    @Test
    void joinReconstructsOriginalText() {
        String text = "alpha\nbeta\ngamma\ndelta";
        assertEquals(text, TextPaginator.join(TextPaginator.paginate(text, 2)));
    }

    @Test
    void joinPreservesTrailingNewline() {
        String text = "a\nb\n";
        assertEquals(text, TextPaginator.join(TextPaginator.paginate(text, 10)));
    }

    @Test
    void emptyTextProducesSingleEmptyPage() {
        List<RenderedPage> pages = TextPaginator.paginate("", 10);
        assertEquals(1, pages.size());
        assertEquals("", pages.get(0).text());
    }

    @Test
    void normalisesWindowsLineEndings() {
        assertEquals("a\nb", TextPaginator.join(TextPaginator.paginate("a\r\nb", 10)));
    }

    @Test
    void rejectsInvalidLinesPerPage() {
        assertThrows(IllegalArgumentException.class, () -> TextPaginator.paginate("x", 0));
    }
}
