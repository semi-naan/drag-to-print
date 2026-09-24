package com.dragnprint.print;

import com.dragnprint.preview.PreviewPane;
import com.dragnprint.preview.RenderedDocument;
import com.dragnprint.ui.Icons;
import com.dragnprint.ui.Theme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public final class PrintPreviewDialog {
    private PrintPreviewDialog() {
    }

    public static PrintResult show(Window owner, RenderedDocument document, PrintService printService, String printerName) {
        Stage stage = new Stage();
        if (owner != null) {
            stage.initOwner(owner);
        }
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Print preview \u2013 " + document.title());

        PreviewPane preview = new PreviewPane();
        preview.setDocument(document);

        Label printerLabel = new Label("Printer: " + printerName);
        Label pageCountLabel = new Label(document.pageCount() + " page(s)");
        TextField rangeField = new TextField("all");
        rangeField.setPrefColumnCount(8);
        Spinner<Integer> copiesSpinner = new Spinner<>(1, 99, 1);
        copiesSpinner.setEditable(true);

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("dnp-error-text");

        Button printButton = new Button("Print");
        printButton.setDefaultButton(true);
        printButton.getStyleClass().add("dnp-primary");
        printButton.setGraphic(Icons.of("dnp-icon-print", 14));
        printButton.setGraphicTextGap(6);
        Button cancelButton = new Button("Cancel");
        cancelButton.setCancelButton(true);

        HBox fields = new HBox(8,
                new Label("Pages:"), rangeField,
                new Label("Copies:"), copiesSpinner,
                new Label("(use 'all' or e.g. 1-3)"));
        fields.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox buttons = new HBox(10, errorLabel, spacer, cancelButton, printButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        BorderPane bottom = new BorderPane();
        bottom.setPadding(new Insets(10));
        bottom.setTop(new HBox(16, printerLabel, pageCountLabel, fields));
        bottom.setBottom(buttons);
        BorderPane.setMargin(buttons, new Insets(8, 0, 0, 0));

        BorderPane root = new BorderPane();
        root.setCenter(preview);
        root.setBottom(bottom);

        final PrintResult[] result = {null};
        printButton.setOnAction(event -> {
            errorLabel.setText("");
            try {
                int copies = PrintOptions.parseCopies(copiesSpinner.getEditor().getText());
                PrintOptions options = PrintOptions.parse(rangeField.getText(), document.pageCount(), copies);
                result[0] = printService.print(document, options);
                stage.close();
            } catch (IllegalArgumentException e) {
                errorLabel.setText(e.getMessage());
            } catch (RuntimeException e) {
                errorLabel.setText("Printing failed: " + e.getMessage());
            }
        });
        cancelButton.setOnAction(event -> stage.close());

        Scene scene = new Scene(root, 920, 680);
        scene.getStylesheets().addAll(Theme.stylesheets());
        stage.setScene(scene);
        stage.showAndWait();
        return result[0];
    }
}
