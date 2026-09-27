package com.okunev.lor.ui;

import com.okunev.lor.model.Protocol;
import com.okunev.lor.service.ProtocolService;
import com.okunev.lor.service.SuggestionService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Set;

public class MainView extends BorderPane {

    private final ObservableList<Protocol> allProtocols;
    private final FilteredList<Protocol> filteredProtocols;
    private final TableView<Protocol> table = new TableView<>();
    private final ProtocolDetailsPane detailsPane = new ProtocolDetailsPane();
    private final Label countLabel = new Label();

    private final TextField searchField = new TextField();
    private final ComboBox<String> populationFilter = new ComboBox<>();
    private final ComboBox<String> sectionFilter = new ComboBox<>();
    private final ToggleButton themeToggle = new ToggleButton("🌙 Тёмная");

    // ===== Автодополнение =====
    private final SuggestionService suggestionService = new SuggestionService();
    private final ContextMenu suggestionsPopup = new ContextMenu();
    private Set<String> allSuggestions;

    private boolean darkMode = false;

    public MainView() {
        ProtocolService service = new ProtocolService();
        List<Protocol> protocols = service.loadProtocols();
        allProtocols = FXCollections.observableArrayList(protocols);
        filteredProtocols = new FilteredList<>(allProtocols, p -> true);

        allSuggestions = suggestionService.buildSuggestions(protocols);

        setupFilters();
        setupTable();
        updateCountLabel();

        setTop(createHeader());
        setCenter(createBody());
        getStyleClass().add("root-pane");
    }

    // ================== HEADER ==================

    private VBox createHeader() {
        Label appTitle = new Label("🩺 Медицинские протоколы");
        appTitle.getStyleClass().add("app-title");

        countLabel.getStyleClass().add("app-subtitle");

        themeToggle.getStyleClass().add("toolbar-button");
        themeToggle.setOnAction(e -> toggleTheme());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox titleBar = new HBox(12, appTitle, countLabel, spacer, themeToggle);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.getStyleClass().add("title-bar");

        HBox filters = createFilterBar();
        filters.getStyleClass().add("filter-bar");

        VBox header = new VBox(titleBar, filters);
        header.getStyleClass().add("header");
        return header;
    }

    private HBox createFilterBar() {
        searchField.setPromptText("🔍 Поиск по названию, лечению, диагностике...");
        searchField.setPrefWidth(420);
        searchField.getStyleClass().add("search-field");

        populationFilter.getItems().addAll("Все", "взрослые", "дети");
        populationFilter.setValue("Все");
        populationFilter.getStyleClass().add("filter-combo");

        sectionFilter.getItems().addAll("Все", "амбулаторно", "стационарно");
        sectionFilter.setValue("Все");
        sectionFilter.getStyleClass().add("filter-combo");

        Label populationLabel = new Label("Население:");
        populationLabel.getStyleClass().add("filter-label");

        Label sectionLabel = new Label("Раздел:");
        sectionLabel.getStyleClass().add("filter-label");

        HBox bar = new HBox(12,
                searchField,
                populationLabel, populationFilter,
                sectionLabel, sectionFilter
        );
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(12, 24, 14, 24));
        return bar;
    }

    // ================== BODY ==================

    private SplitPane createBody() {
        SplitPane split = new SplitPane(table, detailsPane);
        split.setDividerPositions(0.42);
        split.getStyleClass().add("main-split");
        return split;
    }

    // ================== FILTERS ==================

    private void setupFilters() {
        searchField.textProperty().addListener((o, a, b) -> {
            applyFilters();
            updateCountLabel();
            showSuggestions(b);
        });

        populationFilter.valueProperty().addListener((o, a, b) -> {
            applyFilters();
            updateCountLabel();
        });

        sectionFilter.valueProperty().addListener((o, a, b) -> {
            applyFilters();
            updateCountLabel();
        });

        searchField.focusedProperty().addListener((o, a, focused) -> {
            if (!focused) suggestionsPopup.hide();
        });

        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DOWN) {
                if (!suggestionsPopup.isShowing() && !searchField.getText().isBlank()) {
                    showSuggestions(searchField.getText());
                }
                if (suggestionsPopup.isShowing()) {
                    suggestionsPopup.requestFocus();
                    e.consume();
                }
            } else if (e.getCode() == KeyCode.ESCAPE) {
                suggestionsPopup.hide();
                searchField.clear();
            }
        });
    }

    private void showSuggestions(String input) {
        if (input == null || input.isBlank() || input.length() < 2) {
            suggestionsPopup.hide();
            return;
        }

        List<String> matches = suggestionService.filter(allSuggestions, input, 8);

        if (matches.isEmpty()) {
            suggestionsPopup.hide();
            return;
        }

        suggestionsPopup.getItems().clear();
        for (String match : matches) {
            MenuItem item = new MenuItem(match);
            item.setOnAction(e -> {
                searchField.setText(match);
                searchField.positionCaret(match.length());
                suggestionsPopup.hide();
                searchField.requestFocus();
            });
            suggestionsPopup.getItems().add(item);
        }

        if (!suggestionsPopup.isShowing()) {
            suggestionsPopup.show(searchField, Side.BOTTOM, 0, 0);
        }
    }

    private void applyFilters() {
        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        String population = populationFilter.getValue();
        String section = sectionFilter.getValue();

        filteredProtocols.setPredicate(p -> {
            if (!search.isEmpty()) {
                boolean match = contains(p.getName(), search)
                        || contains(p.getDiagRequired(), search)
                        || contains(p.getDiagExtra(), search);
                if (!match) return false;
            }
            if (population != null && !"Все".equals(population) && !population.equals(p.getPopulation())) {
                return false;
            }
            if (section != null && !"Все".equals(section) && !section.equals(p.getSection())) {
                return false;
            }
            return true;
        });
    }

    private boolean contains(String src, String q) {
        return src != null && src.toLowerCase().contains(q);
    }

    private void updateCountLabel() {
        countLabel.setText("Найдено: " + filteredProtocols.size() + " из " + allProtocols.size());
    }

    // ================== TABLE ==================

    private void setupTable() {
        TableColumn<Protocol, String> numCol = new TableColumn<>("№");
        numCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNum()));
        numCol.setPrefWidth(56);
        numCol.setMinWidth(56);
        numCol.setMaxWidth(56);
        numCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Protocol, String> nameCol = new TableColumn<>("Название");
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        nameCol.setMinWidth(300);
        nameCol.setCellFactory(col -> new WrappingCell());

        table.getColumns().addAll(numCol, nameCol);
        table.setItems(filteredProtocols);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getStyleClass().add("protocols-table");
        table.setPlaceholder(new Label("Ничего не найдено"));

        table.setFixedCellSize(-1);

        nameCol.widthProperty().addListener((o, a, b) -> table.refresh());

        table.getSelectionModel().selectedItemProperty().addListener(
                (obs, o, n) -> detailsPane.show(n));
    }

    private static class WrappingCell extends TableCell<Protocol, String> {

        private final Label label = new Label();

        WrappingCell() {
            label.setWrapText(true);
            label.getStyleClass().add("cell-wrapping-label");
            label.setMaxWidth(Double.MAX_VALUE);

            setAlignment(Pos.CENTER_LEFT);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setGraphic(label);
            setPadding(new Insets(8, 12, 8, 12));
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                label.setText(null);
                setGraphic(null);
            } else {
                label.setText(item);
                setGraphic(label);
            }
        }

        @Override
        protected double computePrefHeight(double width) {
            if (isEmpty() || getItem() == null) {
                return super.computePrefHeight(width);
            }
            double cellWidth = getTableColumn().getWidth()
                    - getPadding().getLeft() - getPadding().getRight();
            label.setPrefWidth(cellWidth);
            return label.prefHeight(cellWidth)
                    + getPadding().getTop() + getPadding().getBottom();
        }
    }

    // ================== THEME ==================

    private void toggleTheme() {
        Scene scene = getScene();
        if (scene == null) return;

        darkMode = !darkMode;
        if (darkMode) {
            ThemeManager.applyDarkTheme(scene);
            themeToggle.setText("☀ Светлая");
        } else {
            ThemeManager.applyLightTheme(scene);
            themeToggle.setText("🌙 Тёмная");
        }
    }
}