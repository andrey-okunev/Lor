package com.okunev.lor.service;

import com.okunev.lor.model.ReleaseEntry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ChangelogService {

    private static final String RESOURCE = "/CHANGELOG.md";

    /** Читает и парсит CHANGELOG.md. При любой ошибке — пустой список. */
    public List<ReleaseEntry> load() {
        List<ReleaseEntry> result = new ArrayList<>();
        try (InputStream is = getClass().getResourceAsStream(RESOURCE)) {
            if (is == null) return result;

            try (BufferedReader r = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8))) {

                String version = null;
                List<String> items = new ArrayList<>();
                String line;

                while ((line = r.readLine()) != null) {
                    line = line.strip();
                    if (line.isEmpty()) continue;

                    if (line.startsWith("## ")) {
                        if (version != null && !items.isEmpty()) {
                            result.add(new ReleaseEntry(version, items));
                        }
                        version = line.substring(3).strip();
                        items = new ArrayList<>();
                    } else if (line.startsWith("- ") && version != null) {
                        items.add(line.substring(2).strip());
                    }
                    // остальные строки (проза, комментарии) игнорируем
                }

                if (version != null && !items.isEmpty()) {
                    result.add(new ReleaseEntry(version, items));
                }
            }
        } catch (IOException e) {
            System.err.println("Не удалось прочитать CHANGELOG.md: " + e.getMessage());
        }
        return result;
    }

    /**
     * Записи, чья версия строго больше {@code sinceVersion}.
     * Если {@code sinceVersion} пустой — отдаёт всё (для первого запуска,
     * но мы такое не показываем — см. Main.start()).
     */
    public List<ReleaseEntry> newerThan(String sinceVersion) {
        List<ReleaseEntry> all = load();
        if (sinceVersion == null || sinceVersion.isBlank()) {
            return all;
        }
        List<ReleaseEntry> result = new ArrayList<>();
        for (ReleaseEntry e : all) {
            if (compare(e.version(), sinceVersion) > 0) {
                result.add(e);
            }
        }
        return result;
    }

    /**
     * Semver-подобное сравнение: «1.0.10» > «1.0.9».
     * Нечисловые суффиксы вроде «1.0.60-beta» просто отбрасываются.
     */
    static int compare(String a, String b) {
        String[] pa = a.split("\\.");
        String[] pb = b.split("\\.");
        int n = Math.max(pa.length, pb.length);
        for (int i = 0; i < n; i++) {
            int va = i < pa.length ? parseLeadingInt(pa[i]) : 0;
            int vb = i < pb.length ? parseLeadingInt(pb[i]) : 0;
            if (va != vb) return Integer.compare(va, vb);
        }
        return 0;
    }

    private static int parseLeadingInt(String s) {
        int end = 0;
        while (end < s.length() && Character.isDigit(s.charAt(end))) end++;
        if (end == 0) return 0;
        try {
            return Integer.parseInt(s.substring(0, end));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}