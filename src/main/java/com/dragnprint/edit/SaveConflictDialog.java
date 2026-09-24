package com.dragnprint.edit;

import com.dragnprint.ui.Theme;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.Window;

public final class SaveConflictDialog {
    public enum Choice {
        SAVE_AS,
        OVERWRITE,
        CANCEL
    }

    private SaveConflictDialog() {
    }

    public static Choice show(Window owner, String documentName) {
        Dialog<Choice> dialog = new Dialog<>();
        if (owner != null) {
            dialog.initOwner(owner);
        }
        dialog.setTitle("Save changes");
        dialog.setHeaderText("Save changes to \"" + documentName + "\"?");

        ButtonType saveAsButton = new ButtonType("Save As\u2026", ButtonBar.ButtonData.OTHER);
        ButtonType overwriteButton = new ButtonType("Overwrite", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().setAll(saveAsButton, overwriteButton, cancelButton);
        dialogPane.getStylesheets().addAll(Theme.stylesheets());
        dialogPane.lookupButton(overwriteButton).getStyleClass().add("dnp-primary");
        dialog.setResultConverter(button -> {
            if (button == saveAsButton) {
                return Choice.SAVE_AS;
            }
            if (button == overwriteButton) {
                return Choice.OVERWRITE;
            }
            return Choice.CANCEL;
        });
        return dialog.showAndWait().orElse(Choice.CANCEL);
    }
}
