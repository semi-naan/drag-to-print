package com.dragnprint.edit;

import com.dragnprint.model.DocumentModel;
import com.dragnprint.util.TextStatistics;
import java.util.Objects;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public final class DocumentEditor extends BorderPane {
    private static final PseudoClass MODIFIED = PseudoClass.getPseudoClass("modified");

    private final TextArea textArea = new TextArea();
    private final ToggleButton wrapToggle = new ToggleButton("Wrap");
    private final Label dirtyLabel = new Label();
    private final Label countLabel = new Label();
    private DocumentModel model;
    private boolean internalChange;
    private Runnable onContentChanged;
    private Runnable onSaveRequested;

    public DocumentEditor() {
        textArea.setWrapText(true);
        textArea.getStyleClass().add("dnp-editor");
        textArea.textProperty().addListener((observable, oldValue, newValue) -> handleTextChanged(newValue));

        wrapToggle.setSelected(true);
        wrapToggle.selectedProperty().bindBidirectional(textArea.wrapTextProperty());
        wrapToggle.disableProperty().bind(textArea.editableProperty().not());

        HBox toolbar = new HBox(wrapToggle);
        toolbar.getStyleClass().add("dnp-editor-toolbar");
        toolbar.setAlignment(Pos.CENTER_LEFT);

        dirtyLabel.getStyleClass().add("dnp-editor-dirty");
        countLabel.getStyleClass().add("dnp-editor-count");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox status = new HBox(12, dirtyLabel, spacer, countLabel);
        status.getStyleClass().add("dnp-editor-status");
        status.setAlignment(Pos.CENTER_LEFT);

        setTop(toolbar);
        setCenter(textArea);
        setBottom(status);

        addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
        updateStatus();
    }

    public void setOnContentChanged(Runnable onContentChanged) {
        this.onContentChanged = onContentChanged;
    }

    public void setOnSaveRequested(Runnable onSaveRequested) {
        this.onSaveRequested = onSaveRequested;
    }

    public void setDocument(DocumentModel model) {
        internalChange = true;
        try {
            this.model = model;
            if (model != null && model.isEditable()) {
                if (!Objects.equals(textArea.getText(), model.text())) {
                    textArea.setText(model.text());
                }
                textArea.setEditable(true);
                textArea.setDisable(false);
            } else {
                if (!textArea.getText().isEmpty()) {
                    textArea.setText("");
                }
                textArea.setEditable(false);
                textArea.setDisable(true);
            }
        } finally {
            internalChange = false;
        }
        updateStatus();
    }

    private void handleTextChanged(String newValue) {
        if (!internalChange && model != null && model.isEditable()) {
            model.setText(newValue);
            if (onContentChanged != null) {
                onContentChanged.run();
            }
        }
        updateStatus();
    }

    private void handleKeyPressed(KeyEvent event) {
        if (event.isShortcutDown() && !event.isShiftDown() && !event.isAltDown()
                && event.getCode() == KeyCode.S) {
            if (onSaveRequested != null) {
                onSaveRequested.run();
            }
            event.consume();
        }
    }

    private void updateStatus() {
        if (model == null || !model.isEditable()) {
            dirtyLabel.setText("");
            countLabel.setText("");
            dirtyLabel.pseudoClassStateChanged(MODIFIED, false);
            return;
        }
        boolean dirty = model.isDirty();
        dirtyLabel.setText(dirty ? "Modified" : "Saved");
        dirtyLabel.pseudoClassStateChanged(MODIFIED, dirty);
        String text = textArea.getText() == null ? "" : textArea.getText();
        int words = TextStatistics.wordCount(text);
        int characters = TextStatistics.characterCount(text);
        countLabel.setText(words + (words == 1 ? " word" : " words")
                + " \u2022 " + characters + (characters == 1 ? " character" : " characters"));
    }
}
