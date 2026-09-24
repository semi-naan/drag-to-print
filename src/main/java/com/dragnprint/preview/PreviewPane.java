package com.dragnprint.preview;

import com.dragnprint.ui.Icons;
import java.awt.image.BufferedImage;
import javafx.beans.binding.Bindings;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

public final class PreviewPane extends BorderPane {
    private final StackPane content = new StackPane();
    private final ImageView imageView = new ImageView();
    private final TextArea textArea = new TextArea();
    private final Button previousButton = createNavigationButton("dnp-icon-chevron-left");
    private final Button nextButton = createNavigationButton("dnp-icon-chevron-right");
    private final Label pageLabel = new Label();
    private final HBox navigation;

    private RenderedDocument document;
    private int pageIndex;

    public PreviewPane() {
        getStyleClass().add("preview-pane");

        imageView.setPreserveRatio(true);
        imageView.fitWidthProperty().bind(Bindings.createDoubleBinding(
                () -> Math.max(0, content.getWidth() - 24), content.widthProperty()));
        imageView.fitHeightProperty().bind(Bindings.createDoubleBinding(
                () -> Math.max(0, content.getHeight() - 24), content.heightProperty()));

        textArea.setEditable(false);
        textArea.setWrapText(false);

        content.getStyleClass().add("preview-canvas");
        content.setPadding(new Insets(8));
        content.setAlignment(Pos.CENTER);

        previousButton.setOnAction(event -> showPage(pageIndex - 1));
        nextButton.setOnAction(event -> showPage(pageIndex + 1));

        pageLabel.getStyleClass().add("preview-page-label");

        navigation = new HBox(8, previousButton, pageLabel, nextButton);
        navigation.getStyleClass().add("preview-nav");
        navigation.setAlignment(Pos.CENTER);

        setCenter(content);
        setBottom(navigation);
        clear();
    }

    private static Button createNavigationButton(String iconStyleClass) {
        Button button = new Button();
        button.getStyleClass().add("dnp-icon");
        button.setGraphic(Icons.of(iconStyleClass, 16));
        return button;
    }

    public void setDocument(RenderedDocument document) {
        this.document = document;
        this.pageIndex = 0;
        update();
    }

    public void clear() {
        document = null;
        pageIndex = 0;
        content.getChildren().clear();
        pageLabel.setText("");
        previousButton.setDisable(true);
        nextButton.setDisable(true);
        navigation.setVisible(false);
        navigation.setManaged(false);
    }

    public int pageCount() {
        return document == null ? 0 : document.pageCount();
    }

    public void showPage(int index) {
        if (document == null || document.isEmpty()) {
            return;
        }
        this.pageIndex = Math.max(0, Math.min(index, document.pageCount() - 1));
        update();
    }

    private void update() {
        content.getChildren().clear();
        if (document == null || document.isEmpty()) {
            clear();
            return;
        }
        navigation.setVisible(document.pageCount() > 1);
        navigation.setManaged(document.pageCount() > 1);

        try {
            RenderedPage page = document.page(pageIndex);
            if (page.hasImage()) {
                BufferedImage bufferedImage = page.image();
                Image image = SwingFXUtils.toFXImage(bufferedImage, null);
                imageView.setImage(image);
                content.getChildren().add(imageView);
            } else {
                textArea.setText(page.text());
                content.getChildren().add(textArea);
            }
        } catch (PageRenderException e) {
            Label error = new Label("Could not render this page: " + e.getMessage());
            error.setWrapText(true);
            content.getChildren().add(error);
        }
        pageLabel.setText("Page " + (pageIndex + 1) + " of " + document.pageCount());
        previousButton.setDisable(pageIndex <= 0);
        nextButton.setDisable(pageIndex >= document.pageCount() - 1);
    }
}
