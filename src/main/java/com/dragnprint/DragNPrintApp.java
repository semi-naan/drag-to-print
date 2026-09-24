package com.dragnprint;

import com.dragnprint.ui.MainView;
import com.dragnprint.ui.Theme;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class DragNPrintApp extends Application {
    @Override
    public void start(Stage stage) {
        MainView view = new MainView(stage);
        Scene scene = new Scene(view, 1120, 720);
        scene.getStylesheets().addAll(Theme.stylesheets());
        stage.setTitle("Drag&Print");
        stage.setMinWidth(760);
        stage.setMinHeight(520);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
