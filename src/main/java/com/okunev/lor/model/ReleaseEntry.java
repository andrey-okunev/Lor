package com.okunev.lor.model;

import java.util.List;

/**
 * Одна запись в changelog: версия и список пунктов.
 */
public record ReleaseEntry(String version, List<String> items) {
}