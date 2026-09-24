package com.dragnprint.io;

import com.dragnprint.model.DocumentType;
import com.dragnprint.preview.RenderedDocument;
import com.dragnprint.preview.RenderedPage;
import com.dragnprint.util.TextPaginator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class TextDocumentReader implements DocumentReader {
    @Override
    public boolean supports(DocumentType type) {
        return type == DocumentType.TEXT;
    }

    @Override
    public RenderedDocument read(Path file) throws IOException {
        String text = Files.readString(file, StandardCharsets.UTF_8);
        List<RenderedPage> pages = TextPaginator.paginate(text);
        return new RenderedDocument(file.getFileName().toString(), DocumentType.TEXT, pages);
    }
}
