package com.okunev.lor.ui;

import com.okunev.lor.model.Protocol;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class ProtocolDetailsPane extends ScrollPane {

    private final VBox content = new VBox(16);
    private final Label title = new Label("Выберите протокол слева");
    private final VBox sections = new VBox(14);

    public ProtocolDetailsPane() {
        title.getStyleClass().add("detail-title");
        title.setWrapText(true);
        title.setMaxWidth(Double.MAX_VALUE);
        title.setMinHeight(Region.USE_PREF_SIZE);

        content.setPadding(new Insets(24));
        content.setSpacing(16);
        content.setFillWidth(true);
        content.setMaxWidth(Double.MAX_VALUE);
        content.setMinWidth(0);
        content.getChildren().addAll(title, sections);
        content.getStyleClass().add("detail-root");

        setContent(content);
        setFitToWidth(true);
        setFitToHeight(false);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        getStyleClass().add("detail-scroll");

        viewportBoundsProperty().addListener((obs, oldB, newB) -> {
            if (newB != null && newB.getWidth() > 0) {
                updateContentWidth(newB.getWidth());
            }
        });
    }

    private void updateContentWidth(double viewportWidth) {
        ScrollBar vbar = findVerticalScrollBar();
        double scrollbarWidth = (vbar != null && vbar.isVisible()) ? vbar.getWidth() : 0;
        content.setPrefWidth(Math.max(200, viewportWidth - scrollbarWidth));
    }

    private ScrollBar findVerticalScrollBar() {
        for (var node : lookupAll(".scroll-bar")) {
            if (node instanceof ScrollBar sb && sb.getOrientation() == Orientation.VERTICAL) {
                return sb;
            }
        }
        return null;
    }

    public void show(Protocol p) {
        sections.getChildren().clear();

        if (p == null) {
            title.setText("Выберите протокол слева");
            return;
        }

        title.setText("№" + p.getNum() + ". " + p.getName());

        addMetaBlock(p);
        addSection("Обязательная диагностика", p.getDiagRequired(), "section-diag");
        addSection("Дополнительная диагностика", p.getDiagExtra(), "section-diag-extra");
        addHiddenSection("Лечение", "section-treatment");
        addSection("Примечания", p.getNotes(), "section-notes");
        addSection("Длительность", p.getDuration(), "section-duration");
        addSourceBlock(p);

        setVvalue(0);
    }

    /**
     * Секция с заголовком и кнопкой копирования, но без содержимого.
     * Используется, чтобы показать, что раздел существует, но скрыть данные.
     */
    private void addHiddenSection(String header, String styleClass) {
        Label headerLabel = new Label(header);
        headerLabel.getStyleClass().add("section-header");
        headerLabel.setMinHeight(Region.USE_PREF_SIZE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox headerRow = new HBox(8, headerLabel, spacer);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerRow.setMinHeight(Region.USE_PREF_SIZE);

        Label placeholder = new Label("Содержимое скрыто");
        placeholder.getStyleClass().add("section-hidden-placeholder");
        placeholder.setMaxWidth(Double.MAX_VALUE);

        VBox box = new VBox(8, headerRow, placeholder);
        box.getStyleClass().addAll("section-card", "section-hidden", styleClass);
        box.setMaxWidth(Double.MAX_VALUE);
        box.setMinHeight(Region.USE_PREF_SIZE);
        box.setFillWidth(true);
        sections.getChildren().add(box);
    }

    // ==================== META ====================

    private void addMetaBlock(Protocol p) {
        VBox meta = new VBox(6);
        meta.getStyleClass().add("meta-block");
        meta.setMaxWidth(Double.MAX_VALUE);
        meta.setMinHeight(Region.USE_PREF_SIZE);

        if (notEmpty(p.getPopulation())) {
            meta.getChildren().add(chip("Население: " + p.getPopulation(), "chip-population"));
        }
        if (notEmpty(p.getSection())) {
            meta.getChildren().add(chip("Раздел: " + p.getSection(), "chip-section"));
        }
        if (notEmpty(p.getLevel())) {
            meta.getChildren().add(chip("Уровень: " + p.getLevel(), "chip-level"));
        }

        if (!meta.getChildren().isEmpty()) {
            sections.getChildren().add(meta);
        }
    }

    private Label chip(String text, String styleClass) {
        Label l = new Label(text);
        l.getStyleClass().addAll("chip", styleClass);
        l.setMinHeight(Region.USE_PREF_SIZE);
        return l;
    }

    // ==================== SECTIONS ====================

    private void addSection(String header, String value, String styleClass) {
        if (!notEmpty(value)) return;

        Label headerLabel = new Label(header);
        headerLabel.getStyleClass().add("section-header");
        headerLabel.setMinHeight(Region.USE_PREF_SIZE);

        // === Кнопка копирования в шапке секции ===
        Button copyBtn = createCopyButton(value);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox headerRow = new HBox(8, headerLabel, spacer, copyBtn);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerRow.setMinHeight(Region.USE_PREF_SIZE);

        VBox body = buildBody(value);

        VBox box = new VBox(8, headerRow, body);
        box.getStyleClass().addAll("section-card", styleClass);
        box.setMaxWidth(Double.MAX_VALUE);
        box.setMinHeight(Region.USE_PREF_SIZE);
        box.setFillWidth(true);
        sections.getChildren().add(box);
    }

    /**
     * Создаёт кнопку «Копировать», которая копирует переданный текст
     * в системный буфер и на секунду показывает «Скопировано».
     */
    private Button createCopyButton(String textToCopy) {
        Button btn = new Button("Копировать");
        btn.getStyleClass().add("copy-button");
        btn.setFocusTraversable(false);

        btn.setOnAction(e -> {
            ClipboardContent cc = new ClipboardContent();
            cc.putString(textToCopy);
            Clipboard.getSystemClipboard().setContent(cc);

            btn.setText("Скопировано");
            PauseTransition pause = new PauseTransition(Duration.seconds(1.2));
            pause.setOnFinished(ev -> btn.setText("Копировать"));
            pause.play();
        });

        return btn;
    }

    private VBox buildBody(String value) {
        VBox body = new VBox(6);
        body.setMaxWidth(Double.MAX_VALUE);
        body.setMinHeight(Region.USE_PREF_SIZE);

        String trimmed = value.trim();
        String[] parts = splitNumberedList(trimmed);

        if (parts.length > 1) {
            for (String part : parts) {
                body.getChildren().add(bulletLabel("• " + part.trim()));
            }
        } else {
            body.getChildren().add(bulletLabel(trimmed));
        }

        return body;
    }

    private Label bulletLabel(String text) {
        Label l = new Label(text);
        l.setWrapText(true);
        l.setMaxWidth(Double.MAX_VALUE);
        l.setMinWidth(0);
        l.setMinHeight(Region.USE_PREF_SIZE);
        l.setPrefHeight(Region.USE_COMPUTED_SIZE);
        l.getStyleClass().add("section-body");
        return l;
    }

    private String[] splitNumberedList(String text) {
        String[] parts = text.split("\\s+(?=\\d{1,2}\\.\\s)");
        return parts.length > 1 ? parts : new String[] { text };
    }

    // ==================== SOURCE ====================

    private void addSourceBlock(Protocol p) {
        if (!notEmpty(p.getFile())) return;

        Label headerLabel = new Label("Источник");
        headerLabel.getStyleClass().add("section-header");
        headerLabel.setMinHeight(Region.USE_PREF_SIZE);

        String text = p.getFile();
        if (p.getPage() != null) {
            text += "  •  стр. " + p.getPage();
        }

        Button copyBtn = createCopyButton(text);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox headerRow = new HBox(8, headerLabel, spacer, copyBtn);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerRow.setMinHeight(Region.USE_PREF_SIZE);

        Label bodyLabel = new Label(text);
        bodyLabel.setWrapText(true);
        bodyLabel.setMaxWidth(Double.MAX_VALUE);
        bodyLabel.setMinWidth(0);
        bodyLabel.setMinHeight(Region.USE_PREF_SIZE);
        bodyLabel.setPrefHeight(Region.USE_COMPUTED_SIZE);
        bodyLabel.getStyleClass().add("section-body");

        VBox box = new VBox(8, headerRow, bodyLabel);
        box.getStyleClass().addAll("section-card", "section-source");
        box.setMaxWidth(Double.MAX_VALUE);
        box.setMinHeight(Region.USE_PREF_SIZE);
        sections.getChildren().add(box);
    }

    // ==================== HELPERS ====================

    private boolean notEmpty(String s) {
        return s != null && !s.isBlank();
    }
}