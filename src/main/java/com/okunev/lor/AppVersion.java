package com.okunev.lor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.nio.charset.StandardCharsets;
import java.io.InputStreamReader;

/**
 * Читает версию приложения из version.properties,
 * которую Maven подставляет при сборке.
 */
public final class AppVersion {

    private static final String UNKNOWN = "dev";

    private static final String version;
    private static final String name;

    static {
        Properties props = new Properties();
        try (InputStream is = AppVersion.class.getResourceAsStream("/version.properties")) {
            if (is != null) {
                props.load(new InputStreamReader(is, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            // не критично — покажем "dev"
        }
        version = props.getProperty("app.version", UNKNOWN);
        name = props.getProperty("app.displayName", "Lor");
    }

    private AppVersion() {}

    /** Например, "1.0.5". Локально без Maven-фильтрации — "dev". */
    public static String get() {
        return version;
    }

    /** Читаемое имя приложения. */
    public static String getName() {
        return name;
    }
}