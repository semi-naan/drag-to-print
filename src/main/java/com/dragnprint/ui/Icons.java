package com.dragnprint.ui;

import javafx.scene.layout.Region;

public final class Icons {
    private Icons() {
    }

    public static Region of(String styleClass, double size) {
        Region region = new Region();
        region.getStyleClass().add(styleClass);
        region.setMinSize(size, size);
        region.setPrefSize(size, size);
        region.setMaxSize(size, size);
        return region;
    }
}
