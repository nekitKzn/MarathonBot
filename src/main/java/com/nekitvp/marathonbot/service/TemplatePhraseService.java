package com.nekitvp.marathonbot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Slf4j
@Component
public class TemplatePhraseService {

    public static final String MISSED_TODAY = "missed_today";
    public static final String MISSED_YESTERDAY = "missed_yesterday";
    public static final String EVENING_QUESTIONS = "evening_questions";
    private static final Random RANDOM = new Random();
    private final Map<String, List<String>> phraseMap;

    public TemplatePhraseService() throws IOException {
        phraseMap = Map.of(
                MISSED_TODAY, loadPhrases("missed_report_today.txt"),
                MISSED_YESTERDAY, loadPhrases("missed_report_yesterday.txt"),
                EVENING_QUESTIONS, loadPhrases("evening_questions.txt")
        );
        log.info("Phrases loaded successfully");
    }

    /**
     * Загружает список фраз из указанного файла ресурсов
     */
    private List<String> loadPhrases(String fileName) throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (inputStream == null) {
                throw new IOException("Файл не найден: " + fileName);
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                return reader.lines()
                        .filter(line -> !line.isBlank())
                        .toList();
            }
        }
    }

    // ======== Методы получения фраз ========

    /**
     * 🕓 Для тех, кто не отправил отчёт сегодня
     */
    public String getRandomMissedToday() {
        return randomFromList(MISSED_TODAY);
    }

    /**
     * ⏰ Для тех, кто не отправил отчёт вчера
     */
    public String getRandomMissedYesterday() {
        return randomFromList(MISSED_YESTERDAY);
    }

    /**
     * 🌇 Случайная фраза для вечернего вопроса
     */
    public String getRandomEveningQuestion() {
        return randomFromList(EVENING_QUESTIONS);
    }

    // ======== Вспомогательный метод ========

    private String randomFromList(String key) {
        List<String> phrases = phraseMap.get(key);
        if (phrases == null || phrases.isEmpty()) {
            throw new IllegalStateException("Нет загруженных фраз для ключа: " + key);
        }
        return phrases.get(RANDOM.nextInt(phrases.size()));
    }
}