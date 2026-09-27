package com.okunev.lor.service;

import com.okunev.lor.model.Protocol;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SuggestionService {

    // Слова-«мусор», которые не должны попадать в подсказки
    private static final Set<String> STOP_WORDS = Set.of(
            "например", "далее", "или", "при", "для", "если", "также",
            "по", "на", "в", "с", "и", "не", "из", "от", "до"
    );

    // Минимальная длина слова для подсказки
    private static final int MIN_WORD_LENGTH = 5;

    // Регулярка для слов на кириллице/латинице длиной ≥ 5
    private static final Pattern WORD = Pattern.compile("[А-Яа-яЁёA-Za-z][А-Яа-яЁёA-Za-z\\-]{4,}");

    // Регулярка для кодов МКБ: буква + 2 цифры + точка + цифра(ы)
    private static final Pattern ICD_CODE = Pattern.compile("[A-ZА-Я]\\d{2}(\\.\\d{1,2})?");

    public Set<String> buildSuggestions(List<Protocol> protocols) {
        Set<String> suggestions = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        for (Protocol p : protocols) {
            // Код МКБ из названия
            Matcher icd = ICD_CODE.matcher(safe(p.getName()));
            while (icd.find()) {
                suggestions.add(icd.group());
            }

            // Слова из всех текстовых полей
            collectWords(p.getName(), suggestions);
            collectWords(p.getDiagRequired(), suggestions);
            collectWords(p.getDiagExtra(), suggestions);
            collectWords(p.getTreatment(), suggestions);
        }

        return suggestions;
    }

    private void collectWords(String text, Set<String> out) {
        if (text == null || text.isBlank()) return;
        Matcher m = WORD.matcher(text);
        while (m.find()) {
            String word = m.group();
            if (word.length() < MIN_WORD_LENGTH) continue;
            if (STOP_WORDS.contains(word.toLowerCase())) continue;
            out.add(word);
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    /**
     * Фильтрует подсказки по введённому префиксу.
     * Возвращает не более limit штук.
     */
    public List<String> filter(Set<String> all, String prefix, int limit) {
        if (prefix == null || prefix.isBlank()) return List.of();

        String p = prefix.toLowerCase();
        List<String> result = new ArrayList<>();

        // Сначала — те, что начинаются с префикса
        for (String s : all) {
            if (s.toLowerCase().startsWith(p)) {
                result.add(s);
                if (result.size() >= limit) return result;
            }
        }

        // Потом — те, что содержат префикс внутри
        for (String s : all) {
            if (!s.toLowerCase().startsWith(p) && s.toLowerCase().contains(p)) {
                result.add(s);
                if (result.size() >= limit) return result;
            }
        }

        return result;
    }
}