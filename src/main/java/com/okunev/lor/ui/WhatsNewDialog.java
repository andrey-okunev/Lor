package com.okunev.lor.ui;

import com.okunev.lor.model.ReleaseEntry;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.util.List;

public final class WhatsNewDialog {

    private WhatsNewDialog() {}

    public static void show(Window owner, String currentVersion, List<ReleaseEntry> entries) {
        if (entries == null || entries.isEmpty()) return;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Что нового");
        if (owner != null) dialog.initOwner(owner);

        ButtonType closeBtn = new ButtonType("Закрыть", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeBtn);

        // Переносим таблицы стилей из основного окна
        if (owner != null && owner.getScene() != null) {
            dialog.getDialogPane().getStylesheets()
                    .addAll(owner.getScene().getStylesheets());
        }

        // Ключевой момент: применяем класс .root-pane.
        // Он задаёт -fx-background-color и -fx-text-fill для всей иерархии диалога,
        // поэтому Label-ы и ScrollPane внутри получают правильные цвета темы.
        dialog.getDialogPane().getStyleClass().add("root-pane");

        // Свой заголовок внутри content — вместо системного headerText,
        // который не стилизуется через .root-pane.
        Label title = new Label(headerText(currentVersion, entries));
        title.getStyleClass().add("whats-new-title");
        title.setWrapText(true);
        title.setMaxWidth(Double.MAX_VALUE);

        VBox content = new VBox(16);
        content.setPadding(new Insets(16, 20, 12, 20));
        content.getChildren().add(title);

        for (ReleaseEntry entry : entries) {
            content.getChildren().add(buildReleaseBlock(entry));
        }

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setPrefViewportWidth(520);
        scroll.setPrefViewportHeight(Math.min(420, 100 + entries.size() * 100));
        scroll.getStyleClass().add("detail-scroll"); // уже есть в CSS для обеих тем
        VBox.setVgrow(scroll, Priority.ALWAYS);

        dialog.getDialogPane().setContent(scroll);
        dialog.getDialogPane().setPrefWidth(580);

        dialog.showAndWait();
    }

    private static VBox buildReleaseBlock(ReleaseEntry entry) {
        Label verLabel = new Label("Версия " + entry.version());
        verLabel.getStyleClass().add("whats-new-version");

        VBox itemsBox = new VBox(6);
        for (String item : entry.items()) {
            Label bullet = new Label("•  " + item);
            bullet.getStyleClass().add("whats-new-item");
            bullet.setMaxWidth(Double.MAX_VALUE);
            itemsBox.getChildren().add(bullet);
        }

        VBox block = new VBox(4, verLabel, itemsBox);
        block.getStyleClass().add("section-card"); // фон и радиус берём отсюда
        return block;
    }

    private static String headerText(String currentVersion, List<ReleaseEntry> entries) {
        if (entries.size() == 1) {
            return "Обновление до версии " + currentVersion;
        }
        String oldest = entries.get(entries.size() - 1).version();
        return "Обновление " + oldest + " → " + currentVersion;
    }
}