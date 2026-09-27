module com.okunev.lor {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;

    requires java.desktop;

    opens com.okunev.lor to javafx.fxml, javafx.graphics;
    opens com.okunev.lor.model to com.fasterxml.jackson.databind;

    exports com.okunev.lor;
    exports com.okunev.lor.model;
    exports com.okunev.lor.service;
    exports com.okunev.lor.ui;
}