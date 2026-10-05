package com.okunev.lor;

import com.okunev.lor.model.ReleaseEntry;
import com.okunev.lor.service.ChangelogService;
import com.okunev.lor.service.PreferencesService;
import com.okunev.lor.ui.MainView;
import com.okunev.lor.ui.WhatsNewDialog;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.List;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        Font.loadFont(getClass().getResourceAsStream("/fonts/Inter-Regular.otf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/fonts/Inter-Bold.otf"), 14);

        MainView root = new MainView();
        Scene scene = new Scene(root, 1280, 800);

        stage.setTitle("Медицинские протоколы");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();

        // Показываем «Что нового» только после того, как окно появилось —
        // чтобы диалог корректно отцентрировался относительно owner-окна.
        showWhatsNewIfNeeded(stage);
    }

    /**
     * Показывает changelog, если версия изменилась с прошлого запуска.
     * <p>
     * Логика:
     * <ul>
     *     <li>dev-сборки (без версии) — молча пропускаем;</li>
     *     <li>первый запуск — запоминаем версию, диалог не показываем;</li>
     *     <li>та же версия — ничего не делаем;</li>
     *     <li>новая версия — показываем записи строго новее прошлой.</li>
     * </ul>
     */
    private void showWhatsNewIfNeeded(Stage stage) {
        String current = AppVersion.get();
        if ("dev".equals(current) || current.isBlank()) return;

        PreferencesService prefs = new PreferencesService();
        String lastSeen = prefs.getLastSeenVersion();

        if (lastSeen == null || lastSeen.isBlank()) {
            // Первый запуск: чтобы не выкидывать весь changelog новому пользователю
            prefs.setLastSeenVersion(current);
            return;
        }

        if (current.equals(lastSeen)) return;

        List<ReleaseEntry> entries = new ChangelogService().newerThan(lastSeen);

        // Обновляем в любом случае — даже если записей в CHANGELOG нет.
        prefs.setLastSeenVersion(current);

        if (entries.isEmpty()) return;

        WhatsNewDialog.show(stage, current, entries);
    }

    public static void main(String[] args) {
        launch(args);
    }
}