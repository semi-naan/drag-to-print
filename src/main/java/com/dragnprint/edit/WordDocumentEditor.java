package com.dragnprint.edit;

import com.dragnprint.io.WordDocumentFormat;
import com.dragnprint.model.DocumentModel;
import java.util.List;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public final class WordDocumentEditor extends BorderPane {
    private final VBox paragraphs = new VBox(10);
    private Runnable onContentChanged;
    private Runnable onSaveRequested;

    public WordDocumentEditor() {
        Label guidance = new Label("Edit text within paragraphs. Formatting, tables and embedded content are retained. "
                + "Complex paragraphs are read-only. Preview updates after saving.");
        guidance.setWrapText(true);
        guidance.getStyleClass().add("dnp-editor-status");
        ScrollPane scroll = new ScrollPane(paragraphs);
        scroll.setFitToWidth(true);
        paragraphs.setStyle("-fx-padding: 12;");
        setTop(guidance);
        setCenter(scroll);
        addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.isShortcutDown() && !event.isShiftDown() && !event.isAltDown()
                    && event.getCode() == KeyCode.S) {
                if (onSaveRequested != null) {
                    onSaveRequested.run();
                }
                event.consume();
            }
        });
    }

    public void setOnContentChanged(Runnable onContentChanged) {
        this.onContentChanged = onContentChanged;
    }

    public void setOnSaveRequested(Runnable onSaveRequested) {
        this.onSaveRequested = onSaveRequested;
    }

    public void setDocument(DocumentModel model) {
        paragraphs.getChildren().clear();
        if (model == null) {
            return;
        }
        List<WordDocumentFormat.Paragraph> contents = model.wordParagraphs();
        List<String> edited = model.editedParagraphs();
        for (int i = 0; i < contents.size(); i++) {
            WordDocumentFormat.Paragraph paragraph = contents.get(i);
            Label title = new Label("Paragraph " + (i + 1) + (paragraph.editable() ? "" : " (read-only)"));
            TextArea field = new TextArea(edited.get(i));
            field.setWrapText(true);
            field.setPrefRowCount(2);
            field.setEditable(paragraph.editable());
            field.getStyleClass().add("dnp-editor");
            int index = i;
            if (paragraph.editable()) {
                field.textProperty().addListener((observable, oldValue, value) -> {
                    if (value.contains("\n") || value.contains("\r")) {
                        field.setText(oldValue);
                        return;
                    }
                    model.setParagraphText(index, value);
                    if (onContentChanged != null) {
                        onContentChanged.run();
                    }
                });
            }
            paragraphs.getChildren().add(new VBox(3, title, field));
        }
    }
}
