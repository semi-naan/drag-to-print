package com.dragnprint.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

public final class WordDocumentFormat {
    public record Paragraph(String text, boolean editable) {
    }

    private WordDocumentFormat() {
    }

    public static List<Paragraph> read(Path file) throws IOException {
        try (XWPFDocument document = new XWPFDocument(Files.newInputStream(file))) {
            return paragraphs(document).stream().map(paragraph -> new Paragraph(paragraph.getText(), canEdit(paragraph))).toList();
        } catch (RuntimeException e) {
            throw new IOException("Failed to read DOCX document: " + file.getFileName(), e);
        }
    }

    static List<XWPFParagraph> paragraphs(XWPFDocument document) {
        List<XWPFParagraph> paragraphs = new ArrayList<>();
        collect(document.getBodyElements(), paragraphs);
        return paragraphs;
    }

    private static void collect(List<IBodyElement> elements, List<XWPFParagraph> paragraphs) {
        for (IBodyElement element : elements) {
            if (element instanceof XWPFParagraph paragraph) {
                paragraphs.add(paragraph);
            } else if (element instanceof XWPFTable table) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        collect(cell.getBodyElements(), paragraphs);
                    }
                }
            }
        }
    }

    private static boolean canEdit(XWPFParagraph paragraph) {
        StringBuilder text = new StringBuilder();
        for (XWPFRun run : paragraph.getRuns()) {
            if (run.getCTR().sizeOfTArray() > 1 || !run.getEmbeddedPictures().isEmpty()) {
                return false;
            }
            if (run.getText(0) != null) {
                text.append(run.getText(0));
            }
        }
        return text.toString().equals(paragraph.getText());
    }

    static void replaceText(XWPFParagraph paragraph, String replacement) {
        List<XWPFRun> runs = paragraph.getRuns();
        if (runs.isEmpty()) {
            paragraph.createRun().setText(replacement);
            return;
        }
        String original = paragraph.getText();
        int prefix = 0;
        while (prefix < original.length() && prefix < replacement.length()
                && original.charAt(prefix) == replacement.charAt(prefix)) {
            prefix++;
        }
        int suffix = 0;
        while (suffix < original.length() - prefix && suffix < replacement.length() - prefix
                && original.charAt(original.length() - suffix - 1) == replacement.charAt(replacement.length() - suffix - 1)) {
            suffix++;
        }
        String inserted = replacement.substring(prefix, replacement.length() - suffix);
        int position = 0;
        boolean placed = false;
        for (XWPFRun run : runs) {
            String text = run.getText(0);
            if (text == null) {
                continue;
            }
            int end = position + text.length();
            String before = text.substring(0, Math.max(0, Math.min(text.length(), prefix - position)));
            String after = text.substring(Math.max(0, Math.min(text.length(), original.length() - suffix - position)));
            if (!placed && prefix >= position && prefix <= end) {
                before += inserted;
                placed = true;
            }
            run.setText(before + after, 0);
            position = end;
        }
    }
}
