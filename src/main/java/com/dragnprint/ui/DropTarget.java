package com.dragnprint.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javafx.scene.Node;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

public final class DropTarget {
    private final Node node;
    private final Consumer<List<File>> onFiles;
    private final Consumer<String> onNotice;
    private final List<Node> highlights = new ArrayList<>();

    public DropTarget(Node node, Consumer<List<File>> onFiles, Consumer<String> onNotice) {
        this.node = node;
        this.onFiles = onFiles;
        this.onNotice = onNotice;
        node.addEventFilter(DragEvent.DRAG_OVER, this::handleDragOver);
        node.addEventFilter(DragEvent.DRAG_DROPPED, this::handleDragDropped);
        node.addEventHandler(DragEvent.DRAG_ENTERED, this::handleDragEntered);
        node.addEventHandler(DragEvent.DRAG_EXITED, event -> setDropActive(false));
    }

    public void addHighlight(Node highlight) {
        highlights.add(highlight);
    }

    private void setDropActive(boolean active) {
        applyDropActive(node, active);
        for (Node highlight : highlights) {
            applyDropActive(highlight, active);
        }
    }

    private void applyDropActive(Node target, boolean active) {
        if (active) {
            if (!target.getStyleClass().contains("dnp-drop-active")) {
                target.getStyleClass().add("dnp-drop-active");
            }
        } else {
            target.getStyleClass().remove("dnp-drop-active");
        }
    }

    private void handleDragEntered(DragEvent event) {
        if (isFileOrUrl(event.getDragboard())) {
            setDropActive(true);
        }
    }

    private void handleDragOver(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        if (isFileOrUrl(dragboard)) {
            event.acceptTransferModes(TransferMode.COPY);
            event.consume();
        }
    }

    private boolean isFileOrUrl(Dragboard dragboard) {
        return dragboard.hasFiles() || dragboard.hasUrl();
    }

    private void handleDragDropped(DragEvent event) {
        setDropActive(false);
        Dragboard dragboard = event.getDragboard();
        if (dragboard.hasFiles()) {
            List<File> files = dragboard.getFiles();
            if (!files.isEmpty()) {
                onFiles.accept(files);
                event.setDropCompleted(true);
                event.consume();
            }
        } else if (dragboard.hasUrl()) {
            onNotice.accept("That item is not a local file. Drag a file from your file manager instead.");
            event.setDropCompleted(true);
            event.consume();
        }
    }
}
