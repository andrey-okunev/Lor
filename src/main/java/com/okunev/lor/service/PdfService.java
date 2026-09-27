package com.okunev.lor.service;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class PdfService {

    private final Path pdfCacheDir;

    public PdfService() {
        // Кэш во временной папке — оттуда открываем PDF
        try {
            pdfCacheDir = Files.createTempDirectory("lor_pdfs_");
            pdfCacheDir.toFile().deleteOnExit();
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать временную папку для PDF", e);
        }
    }

    /**
     * Открывает PDF-файл из ресурсов системным просмотрщиком.
     * Если файл уже распакован в кэш — открывает из кэша.
     */
    public void openPdf(String resourceName, int page) {
        try {
            File pdfFile = extractToCache(resourceName);
            openFile(pdfFile);
            // page в данный момент не используется для навигации:
            // большинство PDF-ридеров не поддерживают переход на страницу из командной строки.
            // Номер страницы показывается в UI.
        } catch (Exception e) {
            throw new RuntimeException("Не удалось открыть PDF: " + resourceName, e);
        }
    }

    /**
     * Копирует PDF из classpath в кэш и возвращает File.
     */
    private File extractToCache(String resourceName) throws IOException {
        Path target = pdfCacheDir.resolve(resourceName);

        if (Files.exists(target)) {
            return target.toFile();
        }

        try (InputStream is = getClass().getResourceAsStream("/pdfs/" + resourceName)) {
            if (is == null) {
                throw new IOException("PDF не найден в ресурсах: /pdfs/" + resourceName);
            }
            Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
        }

        return target.toFile();
    }

    /**
     * Открывает файл системным приложением.
     */
    private void openFile(File file) throws IOException {
        if (!Desktop.isDesktopSupported()) {
            throw new IOException("Desktop API не поддерживается на этой платформе");
        }

        Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Desktop.Action.OPEN)) {
            throw new IOException("Открытие файлов не поддерживается");
        }

        desktop.open(file);
    }
}