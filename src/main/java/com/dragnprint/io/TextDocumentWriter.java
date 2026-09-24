package com.dragnprint.io;

import com.dragnprint.model.DocumentType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TextDocumentWriter implements DocumentWriter {
    @Override
    public boolean supports(DocumentType type) {
        return type == DocumentType.TEXT;
    }

    @Override
    public void write(String content, Path target) throws IOException {
        Files.writeString(target, content == null ? "" : content, StandardCharsets.UTF_8);
    }
}
