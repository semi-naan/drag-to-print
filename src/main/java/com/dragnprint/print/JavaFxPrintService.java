package com.dragnprint.print;

import com.dragnprint.preview.PageRenderException;
import com.dragnprint.preview.RenderedDocument;
import com.dragnprint.preview.RenderedPage;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Node;

public final class JavaFxPrintService implements PrintService {
    @Override
    public boolean isPrinterAvailable() {
        return Printer.getDefaultPrinter() != null;
    }

    @Override
    public String printerName() {
        Printer printer = Printer.getDefaultPrinter();
        return printer == null ? "none" : printer.getName();
    }

    @Override
    public PrintResult print(RenderedDocument document, PrintOptions options) {
        if (document == null || document.isEmpty() || options.selectedPageCount() == 0) {
            return PrintResult.failure("Nothing to print");
        }
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            return PrintResult.failure("No printer available");
        }
        Printer printer = job.getPrinter();
        PageLayout layout = printer.createPageLayout(Paper.A4, PageOrientation.PORTRAIT, Printer.MarginType.DEFAULT);
        int printed = 0;
        int failed = 0;
        String failureMessage = null;
        try {
            for (int index = options.fromPage() - 1; index < options.toPage() && index < document.pageCount(); index++) {
                Node node;
                try {
                    RenderedPage page = document.page(index);
                    node = PrintPageRenderer.render(page, layout.getPrintableWidth(), layout.getPrintableHeight());
                } catch (PageRenderException e) {
                    failed += options.copies();
                    if (failureMessage == null) {
                        failureMessage = e.getMessage();
                    }
                    continue;
                }
                if (node == null) {
                    failed += options.copies();
                    continue;
                }
                for (int copy = 0; copy < options.copies(); copy++) {
                    if (job.printPage(layout, node)) {
                        printed++;
                    } else {
                        failed++;
                    }
                }
            }
        } catch (RuntimeException e) {
            failed = Math.max(failed, 1);
            failureMessage = "Printing failed: " + e.getMessage();
        } finally {
            job.endJob();
        }
        if (printed == 0) {
            return PrintResult.failure(failureMessage != null ? failureMessage : "The printer rejected the job");
        }
        if (failed > 0) {
            return PrintResult.partial(printed, failed);
        }
        return PrintResult.success(printed);
    }
}
