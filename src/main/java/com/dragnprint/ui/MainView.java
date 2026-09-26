package com.dragnprint.ui;

import com.dragnprint.edit.DocumentEditor;
import com.dragnprint.edit.SaveConflictDialog;
import com.dragnprint.io.DocumentLoader;
import com.dragnprint.io.DocumentReaders;
import com.dragnprint.io.DocumentWriters;
import com.dragnprint.model.DocumentModel;
import com.dragnprint.model.DocumentType;
import com.dragnprint.preview.PreviewPane;
import com.dragnprint.preview.RenderedDocument;
import com.dragnprint.print.JavaFxPrintService;
import com.dragnprint.print.PrintPreviewDialog;
import com.dragnprint.print.PrintResult;
import com.dragnprint.print.PrintService;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public final class MainView extends StackPane {
    private final Stage stage;
    private final DocumentLoader loader;
    private final DocumentWriters writers;
    private final PrintService printService;

    private final BorderPane shell = new BorderPane();
    private final StackPane toastLayer = new StackPane();
    private final ObservableList<DocumentModel> documents = FXCollections.observableArrayList();
    private final ListView<DocumentModel> documentList = new ListView<>(documents);
    private final PreviewPane previewPane = new PreviewPane();
    private final DocumentEditor editor = new DocumentEditor();
    private final SplitPane mainSplit = new SplitPane();
    private final SplitPane workspaceSplit = new SplitPane();
    private final StackPane centerHost = new StackPane();
    private final Label statusLabel = new Label();
    private final Label documentInfo = new Label();
    private final Button saveButton = new Button("Save");
    private final ProgressIndicator busyIndicator = new ProgressIndicator();
    private final VBox emptyStateCard;
    private final StackPane emptyState;

    private boolean switchingSelection;
    private Task<DocumentLoader.Result> activeLoadTask;

    public MainView(Stage stage) {
        this(stage, new DocumentReaders(), new DocumentWriters(), new JavaFxPrintService());
    }

    public MainView(Stage stage, DocumentReaders readers, DocumentWriters writers, PrintService printService) {
        this.stage = stage;
        this.loader = new DocumentLoader(readers);
        this.writers = writers;
        this.printService = printService;

        emptyStateCard = buildEmptyStateCard();
        emptyState = new StackPane(emptyStateCard);
        emptyState.setAlignment(Pos.CENTER);

        centerHost.getChildren().setAll(emptyState);
        centerHost.setMinWidth(200);

        shell.setTop(buildTopBar());
        shell.setCenter(buildMainArea());
        shell.setBottom(buildStatusBar());

        toastLayer.setMouseTransparent(true);
        toastLayer.setAlignment(Pos.BOTTOM_CENTER);
        getChildren().addAll(shell, toastLayer);

        DropTarget dropTarget = new DropTarget(this, this::openFiles, this::showNotice);
        dropTarget.addHighlight(emptyStateCard);

        documentList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (switchingSelection || oldValue == newValue) {
                return;
            }
            if (oldValue != null && oldValue.isDirty() && !resolveUnsavedChanges(oldValue)) {
                switchingSelection = true;
                try {
                    documentList.getSelectionModel().select(oldValue);
                } finally {
                    switchingSelection = false;
                }
                updateContent(oldValue);
                return;
            }
            updateContent(newValue);
        });

        editor.setOnContentChanged(() -> {
            DocumentModel current = selectedDocument();
            if (current != null) {
                previewPane.setDocument(current.toRendered());
                documentList.refresh();
            }
        });
        editor.setOnSaveRequested(this::saveCurrent);

        stage.setOnCloseRequest(this::handleCloseRequest);
        updateContent(null);
        setPrefSize(1120, 720);
    }

    private Node buildTopBar() {
        Label title = new Label("Drag&Print");
        title.getStyleClass().add("dnp-app-title");

        documentInfo.getStyleClass().add("dnp-document-info");
        documentInfo.setTextOverrun(OverrunStyle.ELLIPSIS);
        documentInfo.setMinWidth(0);

        busyIndicator.setPrefSize(18, 18);
        busyIndicator.setVisible(false);
        busyIndicator.setManaged(false);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        saveButton.setGraphic(Icons.of("dnp-icon-save", 14));
        saveButton.setGraphicTextGap(6);
        saveButton.setDisable(true);
        saveButton.setOnAction(event -> saveCurrent());

        Button printButton = new Button("Print");
        printButton.getStyleClass().add("dnp-primary");
        printButton.setGraphic(Icons.of("dnp-icon-print", 14));
        printButton.setGraphicTextGap(6);
        printButton.setOnAction(event -> printCurrent());

        HBox bar = new HBox(12, title, spacer, busyIndicator, documentInfo, saveButton, printButton);
        bar.getStyleClass().add("dnp-top-bar");
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    private Node buildDocumentPanel() {
        Label header = new Label("OPEN DOCUMENTS");
        header.getStyleClass().add("dnp-panel-title");

        documentList.setPlaceholder(new Label("Drop files here"));
        documentList.setPrefWidth(250);
        documentList.setMinWidth(160);

        Button closeButton = new Button("Close document");
        closeButton.getStyleClass().addAll("dnp-icon", "dnp-wide");
        closeButton.setGraphic(Icons.of("dnp-icon-close", 13));
        closeButton.setGraphicTextGap(6);
        closeButton.setMaxWidth(Double.MAX_VALUE);
        closeButton.setOnAction(event -> closeSelectedDocument());

        VBox box = new VBox(8, header, documentList, closeButton);
        box.getStyleClass().add("dnp-document-panel");
        box.setMaxWidth(360);
        VBox.setVgrow(documentList, Priority.ALWAYS);
        return box;
    }

    private Node buildMainArea() {
        editor.setPrefWidth(420);
        editor.setMinWidth(220);
        previewPane.setMinWidth(200);

        workspaceSplit.setOrientation(Orientation.HORIZONTAL);
        workspaceSplit.getItems().add(previewPane);
        workspaceSplit.setDividerPositions(1.0);

        mainSplit.setOrientation(Orientation.HORIZONTAL);
        mainSplit.getItems().addAll(buildDocumentPanel(), centerHost);
        mainSplit.setDividerPositions(0.22);
        SplitPane.setResizableWithParent(centerHost, Boolean.TRUE);
        return mainSplit;
    }

    private Node buildStatusBar() {
        statusLabel.setMaxWidth(Double.MAX_VALUE);
        statusLabel.getStyleClass().add("dnp-status-bar");
        return statusLabel;
    }

    private VBox buildEmptyStateCard() {
        Region glyph = Icons.of("dnp-icon-drop", 56);
        Label hint = new Label("Drag and drop local files here");
        hint.getStyleClass().add("dnp-drop-title");
        Label detail = new Label("PDF, PNG/JPG/GIF/BMP/TIFF images, .docx, .txt/.md (editable) "
                + "and read-only .doc/.odt. Use the Print button to preview and print.");
        detail.getStyleClass().add("dnp-drop-detail");
        detail.setWrapText(true);
        VBox box = new VBox(10, glyph, hint, detail);
        box.getStyleClass().add("dnp-empty-state");
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private void openFiles(List<File> files) {
        if (files == null || files.isEmpty()) {
            return;
        }
        List<File> toOpen = files.stream().filter(Objects::nonNull).toList();
        if (toOpen.isEmpty()) {
            return;
        }
        if (activeLoadTask != null && activeLoadTask.isRunning()) {
            activeLoadTask.cancel();
        }
        AtomicReference<DocumentLoader.Result> produced = new AtomicReference<>();
        Task<DocumentLoader.Result> task = new Task<>() {
            @Override
            protected DocumentLoader.Result call() {
                DocumentLoader.Result result = loader.load(toOpen, this::isCancelled);
                produced.set(result);
                return result;
            }
        };
        activeLoadTask = task;
        setBusy(true);
        task.setOnSucceeded(event -> {
            DocumentLoader.Result result = task.getValue();
            if (task != activeLoadTask) {
                closeDocuments(result.documents());
                return;
            }
            setBusy(false);
            if (!result.documents().isEmpty()) {
                documents.addAll(result.documents());
                documentList.getSelectionModel().selectLast();
                showSuccess("Opened " + result.documents().size() + " document(s).");
            } else {
                setStatus("No documents were opened.");
            }
            if (!result.notices().isEmpty()) {
                showNotice(String.join("\n", result.notices()));
            }
        });
        task.setOnFailed(event -> {
            if (task != activeLoadTask) {
                return;
            }
            setBusy(false);
            Throwable error = task.getException();
            showError("Could not open files: " + (error == null ? "unknown error" : error.getMessage()));
        });
        task.setOnCancelled(event -> {
            DocumentLoader.Result result = produced.get();
            if (result != null) {
                closeDocuments(result.documents());
            }
            if (task == activeLoadTask) {
                setBusy(false);
            }
        });
        Thread thread = new Thread(task, "dragnprint-loader");
        thread.setDaemon(true);
        thread.start();
    }

    private void closeDocuments(List<DocumentModel> documents) {
        for (DocumentModel document : documents) {
            document.close();
        }
    }

    private void setBusy(boolean busy) {
        busyIndicator.setVisible(busy);
        busyIndicator.setManaged(busy);
    }

    private void updateContent(DocumentModel model) {
        if (model == null) {
            previewPane.clear();
            editor.setDocument(null);
            setEditorVisible(false);
            centerHost.getChildren().setAll(emptyState);
            documentInfo.setText("");
            saveButton.setDisable(true);
            return;
        }
        previewPane.setDocument(model.toRendered());
        centerHost.getChildren().setAll(workspaceSplit);
        saveButton.setDisable(!model.isEditable());
        if (model.isEditable()) {
            editor.setDocument(model);
            setEditorVisible(true);
        } else {
            editor.setDocument(null);
            setEditorVisible(false);
        }
        documentInfo.setText(model.type().description() + "  \u2022  "
                + (model.isEditable() ? "editable" : "preview & print only"));
    }

    private void setEditorVisible(boolean visible) {
        if (visible) {
            if (!workspaceSplit.getItems().contains(editor)) {
                workspaceSplit.getItems().add(editor);
            }
            workspaceSplit.setDividerPositions(0.62);
        } else {
            workspaceSplit.getItems().remove(editor);
            if (!workspaceSplit.getItems().isEmpty()) {
                workspaceSplit.setDividerPositions(1.0);
            }
        }
    }

    private void saveCurrent() {
        DocumentModel model = selectedDocument();
        if (model == null || !model.isEditable()) {
            showNotice("This document cannot be edited or saved.");
            return;
        }
        if (!model.isDirty()) {
            setStatus("No changes to save.");
            return;
        }
        resolveUnsavedChanges(model);
    }

    private void printCurrent() {
        DocumentModel model = selectedDocument();
        if (model == null) {
            showNotice("Open a document before printing.");
            return;
        }
        RenderedDocument rendered = model.toRendered();
        if (rendered.isEmpty()) {
            showNotice("There is nothing to print.");
            return;
        }
        if (!printService.isPrinterAvailable()) {
            showError("No printer is available on this system.");
            return;
        }
        PrintResult result = PrintPreviewDialog.show(stage, rendered, printService, printService.printerName());
        if (result == null) {
            setStatus("Print cancelled.");
        } else if (result.success()) {
            showSuccess(result.message());
        } else {
            showError(result.message());
        }
    }

    private void closeSelectedDocument() {
        DocumentModel model = selectedDocument();
        if (model == null) {
            return;
        }
        if (model.isDirty() && !resolveUnsavedChanges(model)) {
            return;
        }
        documents.remove(model);
        model.close();
        if (documents.isEmpty()) {
            updateContent(null);
        } else {
            documentList.getSelectionModel().selectFirst();
        }
        setStatus("Closed " + model.path().getFileName() + ".");
    }

    private boolean resolveUnsavedChanges(DocumentModel model) {
        SaveConflictDialog.Choice choice = SaveConflictDialog.show(stage, model.displayName());
        return switch (choice) {
            case SAVE_AS -> saveAs(model);
            case OVERWRITE -> overwrite(model);
            case CANCEL -> false;
        };
    }

    private boolean saveAs(DocumentModel model) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save As");
        if (model.path().getFileName() != null) {
            chooser.setInitialFileName(model.path().getFileName().toString());
        }
        if (model.path().getParent() != null) {
            chooser.setInitialDirectory(model.path().getParent().toFile());
        }
        chooser.getExtensionFilters().add(extensionFilter(model.type()));
        File target = chooser.showSaveDialog(stage);
        if (target == null) {
            return false;
        }
        Path targetPath = target.toPath();
        DocumentType targetType = DocumentType.classify(targetPath);
        if (targetType != model.type() || !writers.canWrite(targetType)) {
            showError("Choose a " + expectedExtension(model.type()) + " file for a "
                    + model.type().description() + ".");
            return false;
        }
        return writeTo(model, targetPath, targetType);
    }

    private boolean overwrite(DocumentModel model) {
        if (!writers.canWrite(model.type())) {
            showError("Editing is not supported for this document.");
            return false;
        }
        return writeTo(model, model.path(), model.type());
    }

    private boolean writeTo(DocumentModel model, Path target, DocumentType targetType) {
        try {
            writers.write(model.text(), target, targetType);
            model.setPath(target);
            model.markSaved();
            updateContent(model);
            documentList.refresh();
            showSuccess("Saved " + target.getFileName() + ".");
            return true;
        } catch (Exception e) {
            showError("Could not save " + target.getFileName() + ": " + e.getMessage());
            return false;
        }
    }

    private String expectedExtension(DocumentType type) {
        return type == DocumentType.DOCX ? ".docx" : ".txt or .md";
    }

    private FileChooser.ExtensionFilter extensionFilter(DocumentType type) {
        return switch (type) {
            case TEXT -> new FileChooser.ExtensionFilter("Text files", "*.txt", "*.md", "*.markdown");
            case DOCX -> new FileChooser.ExtensionFilter("Word documents", "*.docx");
            default -> new FileChooser.ExtensionFilter("All files", "*.*");
        };
    }

    private void handleCloseRequest(WindowEvent event) {
        for (DocumentModel model : List.copyOf(documents)) {
            if (model.isDirty() && !resolveUnsavedChanges(model)) {
                event.consume();
                return;
            }
        }
        if (activeLoadTask != null) {
            activeLoadTask.cancel();
        }
        for (DocumentModel model : documents) {
            model.close();
        }
    }

    private DocumentModel selectedDocument() {
        return documentList.getSelectionModel().getSelectedItem();
    }

    private void setStatus(String message) {
        statusLabel.getStyleClass().removeAll("dnp-status-error", "dnp-status-success");
        statusLabel.setText(message);
    }

    private void showNotice(String message) {
        setStatus(message);
        Toast.show(toastLayer, message, Toast.Level.INFO);
    }

    private void showSuccess(String message) {
        statusLabel.getStyleClass().remove("dnp-status-error");
        if (!statusLabel.getStyleClass().contains("dnp-status-success")) {
            statusLabel.getStyleClass().add("dnp-status-success");
        }
        statusLabel.setText(message);
        Toast.show(toastLayer, message, Toast.Level.SUCCESS);
    }

    private void showError(String message) {
        statusLabel.getStyleClass().remove("dnp-status-success");
        if (!statusLabel.getStyleClass().contains("dnp-status-error")) {
            statusLabel.getStyleClass().add("dnp-status-error");
        }
        statusLabel.setText(message);
        Toast.show(toastLayer, message, Toast.Level.ERROR);
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        alert.initOwner(stage);
        alert.setTitle("Drag&Print");
        alert.setHeaderText(null);
        alert.getDialogPane().getStylesheets().addAll(Theme.stylesheets());
        alert.showAndWait();
    }
}
