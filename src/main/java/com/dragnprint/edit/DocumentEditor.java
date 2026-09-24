package com.dragnprint.edit;

import com.dragnprint.model.DocumentModel;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;

public final class DocumentEditor extends BorderPane {
    private final TextArea textArea = new TextArea();
    private DocumentModel model;
    private String savedText;
    private boolean internalChange;
    private Runnable onContentChanged;

    public DocumentEditor() {
        textArea.setWrapText(false);
        textArea.getStyleClass().add("dnp-editor");
        setCenter(textArea);
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (internalChange || model == null || !model.isEditable()) {
                return;
            }
            model.setText(newValue);
            model.setDirty(savedText == null || !savedText.equals(newValue));
            if (onContentChanged != null) {
                onContentChanged.run();
            }
        });
    }

    public void setOnContentChanged(Runnable onContentChanged) {
        this.onContentChanged = onContentChanged;
    }

    public void setDocument(DocumentModel model) {
        internalChange = true;
        try {
            this.model = model;
            if (model != null && model.isEditable()) {
                savedText = model.text();
                textArea.setText(savedText);
                textArea.setEditable(true);
                textArea.setDisable(false);
            } else {
                savedText = null;
                textArea.setText("");
                textArea.setEditable(false);
                textArea.setDisable(true);
            }
        } finally {
            internalChange = false;
        }
    }
}
