package com.okunev.lor.service;

import com.okunev.lor.model.Protocol;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Строит подсказки для строки поиска.
 *
 * Принципы:
 *  - берём только "основные" сущности: названия протоколов и коды МКБ;
 *  - слово попадает в подсказки, только если встречается у ≥ MIN_FREQUENCY протоколов;
 *  - строки длиннее MAX_LENGTH символов отбрасываются (не влезают в поле);
 *  - короткие названия протоколов добавляются целиком как готовая фраза.
 */
public class SuggestionService {

    // Слова, которые не несут смысла в подсказке
    private static final Set<String> STOP_WORDS = Set.of(
            "например", "далее", "или", "при", "для", "если", "также",
            "по", "на", "в", "с", "и", "не", "из", "от", "до",
            "общее", "общий", "общие", "общая",
            "протокол", "протоколы", "раздел",
            "диагностика", "лечение", "терапия", "схема", "курс",
            "уровень", "население", "показания", "примечания"
    );

    private static final int MIN_WORD_LENGTH = 4;
    private static final int MAX_LENGTH      = 40;
    private static final int MIN_FREQUENCY   = 2;

    // Слова (кириллица/латиница, ≥ 3 букв)
    private static final Pattern WORD =
            Pattern.compile("[А-Яа-яЁёA-Za-z][А-Яа-яЁёA-Za-z\\-]{2,}");

    // Код МКБ: буква + 2 цифры + опционально .цифры
    private static final Pattern ICD_CODE =
            Pattern.compile("[A-ZА-Я]\\d{2}(\\.\\d{1,2})?");

    // ============================================================

    public Set<String> buildSuggestions(List<Protocol> protocols) {
        Map<String, Integer> freq = new HashMap<>();

        for (Protocol p : protocols) {
            for (String token : extractTokens(p)) {
                freq.merge(token, 1, Integer::sum);
            }
        }

        Set<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (var e : freq.entrySet()) {
            if (e.getValue() >= MIN_FREQUENCY) {
                result.add(e.getKey());
            }
        }
        return result;
    }

    /**
     * Собирает кандидатов в подсказки из одного протокола.
     */
    private Set<String> extractTokens(Protocol p) {
        Set<String> tokens = new HashSet<>();

        String name = safe(p.getName());

        // 1. Коды МКБ из названия
        Matcher icd = ICD_CODE.matcher(name);
        while (icd.find()) {
            tokens.add(icd.group());
        }

        // 2. Отдельные слова из названия
        collectWords(name, tokens);

        // 3. Короткое название целиком (готовая фраза)
        String trimmed = name.trim();
        if (trimmed.length() >= MIN_WORD_LENGTH
                && trimmed.length() <= MAX_LENGTH
                && !trimmed.isEmpty()) {
            tokens.add(trimmed);
        }

        return tokens;
    }

    private void collectWords(String text, Set<String> out) {
        if (text == null || text.isBlank()) return;

        Matcher m = WORD.matcher(text);
        while (m.find()) {
            String word = m.group();
            if (word.length() < MIN_WORD_LENGTH) continue;
            if (word.length() > MAX_LENGTH)      continue;
            if (STOP_WORDS.contains(word.toLowerCase())) continue;
            out.add(word);
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    // ============================================================

    /**
     * Фильтрует подсказки по префиксу.
     * Сначала — «начинается с», потом — «содержит». Не более {@code limit} штук.
     */
    public List<String> filter(Set<String> all, String prefix, int limit) {
        if (prefix == null || prefix.isBlank()) return List.of();

        String p = prefix.toLowerCase();

        List<String> starts   = new ArrayList<>();
        List<String> contains = new ArrayList<>();

        for (String s : all) {
            String ls = s.toLowerCase();
            if (ls.startsWith(p)) {
                starts.add(s);
                if (starts.size() >= limit) break;   // уже набрали
            } else if (ls.contains(p)) {
                contains.add(s);
            }
        }

        List<String> result = new ArrayList<>(starts);
        for (String s : contains) {
            if (result.size() >= limit) break;
            result.add(s);
        }
        return result;
    }
}