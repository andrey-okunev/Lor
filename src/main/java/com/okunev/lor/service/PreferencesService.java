package com.okunev.lor.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public final class PreferencesService {

    private static final Path DIR  = Paths.get(System.getProperty("user.home"), ".lor");
    private static final Path FILE = DIR.resolve("prefs.properties");

    private static final String KEY_THEME = "theme";

    private static final String KEY_LAST_SEEN_VERSION = "lastSeenVersion";

    private final Properties props = new Properties();

    public PreferencesService() {
        try {
            if (Files.exists(FILE)) {
                try (var in = Files.newInputStream(FILE)) {
                    props.load(in);
                }
            }
        } catch (IOException e) {
            System.err.println("Не удалось прочитать настройки: " + e.getMessage());
        }
    }

    /** Версия, которую пользователь уже видел. Пустая строка — не показывали. */
    public String getLastSeenVersion() {
        return props.getProperty(KEY_LAST_SEEN_VERSION, "");
    }

    public void setLastSeenVersion(String version) {
        props.setProperty(KEY_LAST_SEEN_VERSION, version == null ? "" : version);
        save();
    }

    public boolean isDark() {
        return "dark".equalsIgnoreCase(props.getProperty(KEY_THEME, "light"));
    }

    public void setDark(boolean dark) {
        props.setProperty(KEY_THEME, dark ? "dark" : "light");
        save();
    }

    private void save() {
        try {
            Files.createDirectories(DIR);
            try (var out = Files.newOutputStream(FILE)) {
                props.store(out, "Lor preferences");
            }
        } catch (IOException e) {
            System.err.println("Не удалось сохранить настройки: " + e.getMessage());
        }
    }
}