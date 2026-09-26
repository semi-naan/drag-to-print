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
    private static final double CANVAS_PADDING = 8;
    private static final double IMAGE_CARD_PADDING = 8;
    private static final double IMAGE_CARD_BORDER = 1;
    private static final double IMAGE_INSET =
            2 * (CANVAS_PADDING + IMAGE_CARD_PADDING + IMAGE_CARD_BORDER);
    private static final double TEXT_CARD_PADDING = 6;
    private static final double TEXT_MAX_WIDTH = 720;
    private static final double TEXT_MAX_HEIGHT = 800;

    private final StackPane content = new StackPane();
    private final ImageView imageView = new ImageView();
    private final TextArea textArea = new TextArea();
    private final StackPane imageCard = new StackPane(imageView);
    private final StackPane textCard = new StackPane(textArea);
    private final Button previousButton = createNavigationButton("dnp-icon-chevron-left");
    private final Button nextButton = createNavigationButton("dnp-icon-chevron-right");
    private final Label pageLabel = new Label();
    private final HBox navigation;

    private RenderedDocument document;
    private int pageIndex;

    public PreviewPane() {
        getStyleClass().add("preview-pane");

        content.getStyleClass().add("preview-canvas");
        content.setPadding(new Insets(CANVAS_PADDING));
        content.setAlignment(Pos.CENTER);
        content.setMinSize(0, 0);

        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.fitWidthProperty().bind(Bindings.createDoubleBinding(
                () -> imageFitWidth(), content.widthProperty(), content.heightProperty(),
                imageView.imageProperty()));
        imageView.fitHeightProperty().bind(Bindings.createDoubleBinding(
                () -> imageFitHeight(), content.widthProperty(), content.heightProperty(),
                imageView.imageProperty()));

        imageCard.getStyleClass().add("preview-page-card");
        imageCard.setPadding(new Insets(IMAGE_CARD_PADDING));
        imageCard.setMinSize(0, 0);
        imageCard.maxWidthProperty().bind(imageView.fitWidthProperty().add(2 * (IMAGE_CARD_PADDING + IMAGE_CARD_BORDER)));
        imageCard.maxHeightProperty().bind(imageView.fitHeightProperty().add(2 * (IMAGE_CARD_PADDING + IMAGE_CARD_BORDER)));

        textArea.setEditable(false);
        textArea.setWrapText(false);
        textArea.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        textCard.getStyleClass().add("preview-page-card");
        textCard.setPadding(new Insets(TEXT_CARD_PADDING));
        textCard.setMinSize(0, 0);
        textCard.maxWidthProperty().bind(Bindings.createDoubleBinding(
                () -> textCardWidth(), content.widthProperty()));
        textCard.maxHeightProperty().bind(Bindings.createDoubleBinding(
                () -> textCardHeight(), content.heightProperty()));

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

    private double imageFitWidth() {
        Image image = imageView.getImage();
        return image == null ? 0 : image.getWidth() * imageScale(image);
    }

    private double imageFitHeight() {
        Image image = imageView.getImage();
        return image == null ? 0 : image.getHeight() * imageScale(image);
    }

    private double imageScale(Image image) {
        double availableWidth = content.getWidth() - IMAGE_INSET;
        double availableHeight = content.getHeight() - IMAGE_INSET;
        if (image.getWidth() <= 0 || image.getHeight() <= 0
                || availableWidth <= 0 || availableHeight <= 0) {
            return 0;
        }
        return Math.min(Math.min(availableWidth / image.getWidth(), availableHeight / image.getHeight()), 1.0);
    }

    private double textCardWidth() {
        return Math.max(0, Math.min(TEXT_MAX_WIDTH, content.getWidth() - 2 * CANVAS_PADDING));
    }

    private double textCardHeight() {
        return Math.max(0, Math.min(TEXT_MAX_HEIGHT, content.getHeight() - 2 * CANVAS_PADDING));
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
                content.getChildren().add(imageCard);
            } else {
                textArea.setText(page.text());
                content.getChildren().add(textCard);
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
