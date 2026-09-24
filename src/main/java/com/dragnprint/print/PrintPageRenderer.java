package com.dragnprint.print;

import com.dragnprint.preview.RenderedPage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public final class PrintPageRenderer {
    private static final double TEXT_FONT_SIZE = 10.0;

    private PrintPageRenderer() {
    }

    public static Node render(RenderedPage page, double maxWidth, double maxHeight) {
        if (page.hasImage()) {
            Image image = SwingFXUtils.toFXImage(page.image(), null);
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            double width = image.getWidth();
            double height = image.getHeight();
            if (width > 0 && height > 0) {
                double scale = Math.min(maxWidth / width, maxHeight / height);
                imageView.setFitWidth(width * scale);
                imageView.setFitHeight(height * scale);
            }
            StackPane holder = new StackPane(imageView);
            holder.setPrefSize(imageView.getFitWidth(), imageView.getFitHeight());
            return holder;
        }
        if (page.hasText()) {
            Text text = new Text(page.text());
            text.setWrappingWidth(maxWidth);
            text.setFont(Font.font("Monospaced", TEXT_FONT_SIZE));
            TextFlow flow = new TextFlow(text);
            flow.setPrefWidth(maxWidth);
            flow.setMaxWidth(maxWidth);
            return flow;
        }
        return null;
    }
}
