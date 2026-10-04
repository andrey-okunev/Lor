package com.okunev.lor;

import com.okunev.lor.ui.MainView;
import com.okunev.lor.ui.ThemeManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        Font.loadFont(getClass().getResourceAsStream("/fonts/Inter-Regular.otf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/fonts/Inter-Bold.otf"), 14);

        MainView root = new MainView();

        Scene scene = new Scene(root, 1280, 800);
        ThemeManager.applyLightTheme(scene);

        stage.setTitle("Медицинские протоколы");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}