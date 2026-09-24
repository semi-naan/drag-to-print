package com.dragnprint.print;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dragnprint.model.DocumentType;
import com.dragnprint.preview.RenderedDocument;
import com.dragnprint.preview.RenderedPage;
import java.util.List;
import org.junit.jupiter.api.Test;

class FakePrintServiceTest {
    private RenderedDocument document(int pages) {
        return new RenderedDocument("doc.pdf", DocumentType.PDF,
                java.util.stream.IntStream.range(0, pages)
                        .mapToObj(i -> RenderedPage.ofText("page " + i))
                        .toList());
    }

    @Test
    void printsSelectedPagesAndCopies() {
        FakePrintService service = new FakePrintService();

        PrintResult result = service.print(document(5), PrintOptions.parse("2-4", 5, 2));

        assertTrue(result.success());
        assertEquals(6, result.pagesPrinted());
        assertEquals(1, service.jobsCount());
        assertNotNull(service.lastJob());
        assertEquals(PrintOptions.parse("2-4", 5, 2), service.lastJob().options());
    }

    @Test
    void reportsUnavailablePrinter() {
        FakePrintService service = new FakePrintService();
        service.setAvailable(false);

        PrintResult result = service.print(document(3), PrintOptions.all(3, 1));

        assertFalse(result.success());
        assertEquals("No printer available", result.message());
        assertFalse(service.isPrinterAvailable());
        assertEquals(1, service.jobsCount());
        assertFalse(service.lastJob().result().success());
    }

    @Test
    void refusesEmptyDocuments() {
        FakePrintService service = new FakePrintService();

        PrintResult result = service.print(new RenderedDocument("empty.txt", DocumentType.TEXT, List.of()),
                PrintOptions.all(0, 1));

        assertFalse(result.success());
        assertEquals("Nothing to print", result.message());
    }

    @Test
    void keepsHistoryOfJobs() {
        FakePrintService service = new FakePrintService();
        service.print(document(1), PrintOptions.all(1, 1));
        service.print(document(2), PrintOptions.all(2, 1));

        assertEquals(2, service.jobsCount());
        assertEquals(1, service.jobs().get(0).document().pageCount());
        assertEquals(2, service.jobs().get(1).document().pageCount());
    }

    @Test
    void exposesAPrinterName() {
        assertEquals("Fake printer", new FakePrintService().printerName());
    }
}
