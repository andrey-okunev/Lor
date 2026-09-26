package com.okunev.lor.ui;

import javafx.scene.Scene;

public final class ThemeManager {

    private static final String LIGHT = "/light-theme.css";
    private static final String DARK = "/dark-theme.css";

    private ThemeManager() {}

    public static void applyLightTheme(Scene scene) {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(getResource(LIGHT));
    }

    public static void applyDarkTheme(Scene scene) {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(getResource(DARK));
    }

    private static String getResource(String path) {
        var url = ThemeManager.class.getResource(path);
        if (url == null) {
            System.err.println("CSS не найден: " + path);
            return "";
        }
        return url.toExternalForm();
    }
}