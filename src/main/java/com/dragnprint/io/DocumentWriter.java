package com.dragnprint.io;

import com.dragnprint.model.DocumentType;
import java.io.IOException;
import java.nio.file.Path;

public interface DocumentWriter {
    boolean supports(DocumentType type);

    void write(String content, Path target) throws IOException;
}
