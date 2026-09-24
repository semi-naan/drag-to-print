package com.dragnprint.io;

import com.dragnprint.model.DocumentType;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

public final class WordDocumentWriter implements DocumentWriter {
    @Override
    public boolean supports(DocumentType type) {
        return type == DocumentType.DOCX;
    }

    @Override
    public void write(String content, Path target) throws IOException {
        String normalized = content == null ? "" : content.replace("\r\n", "\n").replace('\r', '\n');
        try (XWPFDocument document = new XWPFDocument();
                OutputStream output = Files.newOutputStream(target)) {
            for (String line : normalized.split("\n", -1)) {
                XWPFParagraph paragraph = document.createParagraph();
                XWPFRun run = paragraph.createRun();
                run.setText(line);
            }
            document.write(output);
        }
    }
}
