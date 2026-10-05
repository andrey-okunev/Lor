package com.okunev.lor.ui;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Минимальный Markdown-рендер для текста протокола.
 * <p>
 * Поддерживает: {@code # ## ###}, {@code - item}, {@code 1. item},
 * {@code **жирный**}, {@code *курсив*}. Всё остальное — как обычный текст.
 * Не претендует на полноту CommonMark — только то, что реально встречается
 * в наших JSON-протоколах.
 */
public final class MarkdownRenderer {

    private MarkdownRenderer() {}

    private static final Pattern HEADING = Pattern.compile("^(#{1,3})\\s+(.+)$");
    private static final Pattern BULLET  = Pattern.compile("^[-*]\\s+(.+)$");
    private static final Pattern NUMBER  = Pattern.compile("^\\d{1,2}[.)]\\s+(.+)$");
    private static final Pattern INLINE  = Pattern.compile("(\\*\\*[^*]+\\*\\*|\\*[^*]+\\*)");

    /** Возвращает VBox с полностью готовой разметкой. */
    public static VBox render(String markdown) {
        VBox box = new VBox(6);
        box.setMaxWidth(Double.MAX_VALUE);
        box.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);

        if (markdown == null || markdown.isBlank()) return box;

        String[] lines = markdown.split("\\r?\\n");
        StringBuilder paragraph = new StringBuilder();

        for (String raw : lines) {
            String line = raw.strip();

            if (line.isEmpty()) {
                flushParagraph(box, paragraph);
                continue;
            }

            Matcher h = HEADING.matcher(line);
            if (h.matches()) {
                flushParagraph(box, paragraph);
                int level = h.group(1).length();
                box.getChildren().add(heading(h.group(2), level));
                continue;
            }

            Matcher b = BULLET.matcher(line);
            if (b.matches()) {
                flushParagraph(box, paragraph);
                box.getChildren().add(bullet(b.group(1), "•"));
                continue;
            }

            Matcher n = NUMBER.matcher(line);
            if (n.matches()) {
                flushParagraph(box, paragraph);
                box.getChildren().add(bullet(n.group(1), null));
                continue;
            }

            // обычная строка — копим в параграф
            if (paragraph.length() > 0) paragraph.append(' ');
            paragraph.append(line);
        }
        flushParagraph(box, paragraph);
        return box;
    }

    // ============================================================

    private static void flushParagraph(VBox box, StringBuilder sb) {
        if (sb.length() == 0) return;
        TextFlow tf = new TextFlow();
        tf.setMaxWidth(Double.MAX_VALUE);
        tf.setMinWidth(0);
        tf.getChildren().addAll(inlineText(sb.toString(), "section-body"));
        box.getChildren().add(tf);
        sb.setLength(0);
    }

    private static Node heading(String text, int level) {
        Label l = new Label(text);
        l.setWrapText(true);
        l.setMaxWidth(Double.MAX_VALUE);
        l.setMinWidth(0);
        l.getStyleClass().add(level == 1 ? "md-h1" : level == 2 ? "md-h2" : "md-h3");
        return l;
    }

    private static TextFlow bullet(String text, String marker) {
        TextFlow tf = new TextFlow();
        tf.setMaxWidth(Double.MAX_VALUE);
        tf.setMinWidth(0);
        tf.setPadding(new javafx.geometry.Insets(0, 0, 0, marker != null ? 12 : 0));
        if (marker != null) {
            Text m = new Text(marker + "  ");
            m.getStyleClass().add("section-body");
            tf.getChildren().add(m);
        }
        tf.getChildren().addAll(inlineText(text, "section-body"));
        return tf;
    }

    /**
     * Разбивает строку на обычные и жирные/курсивные {@link Text}-узлы.
     */
    private static List<Text> inlineText(String s, String baseClass) {
        List<Text> out = new ArrayList<>();
        Matcher m = INLINE.matcher(s);
        int pos = 0;
        while (m.find()) {
            if (m.start() > pos) {
                out.add(styled(s.substring(pos, m.start()), baseClass, null));
            }
            String token = m.group();
            if (token.startsWith("**")) {
                out.add(styled(token.substring(2, token.length() - 2), baseClass, "md-bold"));
            } else {
                out.add(styled(token.substring(1, token.length() - 1), baseClass, "md-italic"));
            }
            pos = m.end();
        }
        if (pos < s.length()) {
            out.add(styled(s.substring(pos), baseClass, null));
        }
        if (out.isEmpty()) out.add(styled("", baseClass, null));
        return out;
    }

    private static Text styled(String s, String base, String extra) {
        Text t = new Text(s);
        t.getStyleClass().add(base);
        if (extra != null) t.getStyleClass().add(extra);
        return t;
    }
}