package com.dragnprint.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class WordDocumentFormatTest {
    @TempDir
    Path tempDir;

    @Test
    void editsParagraphsWithoutFlatteningStylesOrTablesAndSupportsSubsequentSaves() throws IOException {
        Path source = tempDir.resolve("source.docx");
        try (XWPFDocument document = new XWPFDocument()) {
            XWPFParagraph paragraph = document.createParagraph();
            paragraph.setAlignment(ParagraphAlignment.CENTER);
            paragraph.createRun().setText("Hello ");
            var bold = paragraph.createRun();
            bold.setBold(true);
            bold.setText("world");
            XWPFTable table = document.createTable(1, 1);
            table.getRow(0).getCell(0).setText("In a cell");
            try (OutputStream output = Files.newOutputStream(source)) {
                document.write(output);
            }
        }

        List<WordDocumentFormat.Paragraph> original = WordDocumentFormat.read(source);
        assertEquals("Hello world", original.get(0).text());
        List<String> edited = new ArrayList<>(original.stream().map(WordDocumentFormat.Paragraph::text).toList());
        edited.set(0, "Hello worlds!");
        int cellIndex = edited.indexOf("In a cell");
        edited.set(cellIndex, "In a table cell");
        Path copy = tempDir.resolve("copy.docx");
        WordDocumentWriter writer = new WordDocumentWriter();
        writer.writePreservingFormat(source, original, edited, copy);

        try (XWPFDocument unchanged = new XWPFDocument(Files.newInputStream(source));
                XWPFDocument saved = new XWPFDocument(Files.newInputStream(copy))) {
            assertEquals("Hello world", unchanged.getParagraphArray(0).getText());
            XWPFParagraph paragraph = saved.getParagraphArray(0);
            assertEquals("Hello worlds!", paragraph.getText());
            assertEquals(ParagraphAlignment.CENTER, paragraph.getAlignment());
            assertTrue(paragraph.getRuns().get(1).isBold());
            assertEquals("worlds!", paragraph.getRuns().get(1).getText(0));
            assertEquals("In a table cell", saved.getTables().getFirst().getRow(0).getCell(0).getText());
        }

        List<WordDocumentFormat.Paragraph> saved = WordDocumentFormat.read(copy);
        List<String> secondEdit = new ArrayList<>(saved.stream().map(WordDocumentFormat.Paragraph::text).toList());
        secondEdit.set(0, "Hello friend!");
        writer.writePreservingFormat(copy, saved, secondEdit, copy);
        assertEquals("Hello friend!", WordDocumentFormat.read(copy).getFirst().text());
    }

    @Test
    void protectsParagraphsWithNonTextContentAndRejectsOutdatedSource() throws IOException {
        Path source = tempDir.resolve("complex.docx");
        try (XWPFDocument document = new XWPFDocument()) {
            XWPFParagraph paragraph = document.createParagraph();
            paragraph.createRun().setText("before");
            paragraph.createRun().addBreak();
            try (OutputStream output = Files.newOutputStream(source)) {
                document.write(output);
            }
        }
        List<WordDocumentFormat.Paragraph> original = WordDocumentFormat.read(source);
        assertFalse(original.getFirst().editable());
        assertThrows(IOException.class, () -> new WordDocumentWriter().writePreservingFormat(
                source, original, List.of("replacement"), tempDir.resolve("blocked.docx")));

        try (XWPFDocument document = new XWPFDocument()) {
            document.createParagraph().createRun().setText("different");
            try (OutputStream output = Files.newOutputStream(source)) {
                document.write(output);
            }
        }
        assertThrows(IOException.class, () -> new WordDocumentWriter().writePreservingFormat(
                source, original, List.of(original.getFirst().text()), tempDir.resolve("stale.docx")));
    }
}
