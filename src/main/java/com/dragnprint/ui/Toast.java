package com.dragnprint.ui;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public final class Toast {
    public enum Level {
        INFO,
        SUCCESS,
        ERROR
    }

    private static final Object ACTIVE_TOAST_KEY = new Object();
    private static final Object ACTIVE_ANIMATION_KEY = new Object();

    private Toast() {
    }

    public static void show(StackPane layer, String message, Level level) {
        clearActive(layer);

        Label toast = new Label(message);
        toast.getStyleClass().add("dnp-toast");
        if (level == Level.SUCCESS) {
            toast.getStyleClass().add("dnp-toast-success");
        } else if (level == Level.ERROR) {
            toast.getStyleClass().add("dnp-toast-error");
        }
        toast.setWrapText(true);
        toast.setMaxWidth(520);
        StackPane.setAlignment(toast, Pos.BOTTOM_CENTER);
        StackPane.setMargin(toast, new Insets(0, 0, 52, 0));

        layer.getChildren().add(toast);
        layer.getProperties().put(ACTIVE_TOAST_KEY, toast);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(120), toast);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        PauseTransition pause = new PauseTransition(Duration.millis(3500));
        FadeTransition fadeOut = new FadeTransition(Duration.millis(250), toast);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        SequentialTransition sequence = new SequentialTransition(fadeIn, pause, fadeOut);
        layer.getProperties().put(ACTIVE_ANIMATION_KEY, sequence);
        sequence.setOnFinished(event -> {
            layer.getChildren().remove(toast);
            if (layer.getProperties().get(ACTIVE_TOAST_KEY) == toast) {
                layer.getProperties().remove(ACTIVE_TOAST_KEY);
                layer.getProperties().remove(ACTIVE_ANIMATION_KEY);
            }
        });
        sequence.play();
    }

    private static void clearActive(StackPane layer) {
        Object animation = layer.getProperties().remove(ACTIVE_ANIMATION_KEY);
        if (animation instanceof Animation active) {
            active.stop();
        }
        Object toast = layer.getProperties().remove(ACTIVE_TOAST_KEY);
        if (toast instanceof Label label) {
            layer.getChildren().remove(label);
        }
    }
}
