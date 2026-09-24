package com.dragnprint.print;

import com.dragnprint.preview.RenderedDocument;

public interface PrintService {
    boolean isPrinterAvailable();

    String printerName();

    PrintResult print(RenderedDocument document, PrintOptions options);
}
