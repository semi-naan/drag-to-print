package com.dragnprint.io;

import com.dragnprint.model.DocumentType;
import com.dragnprint.preview.RenderedDocument;
import java.io.IOException;
import java.nio.file.Path;

public interface DocumentReader {
    boolean supports(DocumentType type);

    RenderedDocument read(Path file) throws IOException;
}
