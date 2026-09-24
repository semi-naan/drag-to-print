package com.dragnprint.print;

import com.dragnprint.preview.RenderedDocument;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class FakePrintService implements PrintService {
    private boolean available = true;
    private final List<PrintJob> jobs = new ArrayList<>();

    public record PrintJob(RenderedDocument document, PrintOptions options, PrintResult result) {
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public List<PrintJob> jobs() {
        return List.copyOf(jobs);
    }

    public PrintJob lastJob() {
        return jobs.isEmpty() ? null : jobs.get(jobs.size() - 1);
    }

    public int jobsCount() {
        return jobs.size();
    }

    @Override
    public boolean isPrinterAvailable() {
        return available;
    }

    @Override
    public String printerName() {
        return "Fake printer";
    }

    @Override
    public PrintResult print(RenderedDocument document, PrintOptions options) {
        Objects.requireNonNull(document, "document");
        Objects.requireNonNull(options, "options");
        PrintResult result;
        if (!available) {
            result = PrintResult.failure("No printer available");
        } else if (document.isEmpty() || options.selectedPageCount() == 0) {
            result = PrintResult.failure("Nothing to print");
        } else {
            result = PrintResult.success(options.selectedPageCount() * options.copies());
        }
        jobs.add(new PrintJob(document, options, result));
        return result;
    }
}
