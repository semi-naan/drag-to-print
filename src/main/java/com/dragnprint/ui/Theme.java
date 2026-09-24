package com.dragnprint.ui;

import java.net.URL;
import java.util.List;

public final class Theme {
    public static final String STYLESHEET = "/com/dragnprint/dragnprint.css";

    private Theme() {
    }

    public static List<String> stylesheets() {
        URL resource = Theme.class.getResource(STYLESHEET);
        if (resource == null) {
            System.err.println("Drag&Print: stylesheet not found at " + STYLESHEET);
            return List.of();
        }
        return List.of(resource.toExternalForm());
    }
}
