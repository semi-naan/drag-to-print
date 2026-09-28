package com.dragnprint.io;

import com.dragnprint.model.DocumentType;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;
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
                OutputStream output = Files.newOutputStream(target, StandardOpenOption.CREATE_NEW)) {
            for (String line : normalized.split("\n", -1)) {
                XWPFParagraph paragraph = document.createParagraph();
                XWPFRun run = paragraph.createRun();
                run.setText(line);
            }
            document.write(output);
        }
    }

    public void writePreservingFormat(Path source, List<WordDocumentFormat.Paragraph> original,
            List<String> edited, Path target) throws IOException {
        if (original.size() != edited.size()) {
            throw new IOException("The DOCX paragraph structure has changed.");
        }
        Path destination = target.toAbsolutePath();
        Path temporary = null;
        try {
            try (XWPFDocument document = new XWPFDocument(Files.newInputStream(source))) {
                List<XWPFParagraph> paragraphs = WordDocumentFormat.paragraphs(document);
                if (paragraphs.size() != original.size()) {
                    throw new IOException("The DOCX paragraph structure has changed on disk.");
                }
                for (int i = 0; i < paragraphs.size(); i++) {
                    XWPFParagraph paragraph = paragraphs.get(i);
                    WordDocumentFormat.Paragraph baseline = original.get(i);
                    if (!paragraph.getText().equals(baseline.text())) {
                        throw new IOException("The DOCX content has changed on disk.");
                    }
                    if (!baseline.text().equals(edited.get(i))) {
                        if (!baseline.editable()) {
                            throw new IOException("This DOCX paragraph cannot be edited without changing its structure.");
                        }
                        WordDocumentFormat.replaceText(paragraph, edited.get(i));
                    }
                }
                temporary = Files.createTempFile(destination.getParent(), ".dragnprint-", ".docx");
                try (OutputStream output = Files.newOutputStream(temporary)) {
                    document.write(output);
                }
            }
            Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (RuntimeException e) {
            throw new IOException("Failed to save DOCX document: " + target.getFileName(), e);
        } finally {
            if (temporary != null) {
                Files.deleteIfExists(temporary);
            }
        }
    }
}
