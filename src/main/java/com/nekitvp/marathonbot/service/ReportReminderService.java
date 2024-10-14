package com.nekitvp.marathonbot.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import org.springframework.stereotype.Component;

@Component
public class ReportReminderService {

    private final List<String> phrases;
    private static final Random random = new Random();

    public ReportReminderService() throws IOException {

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("phrases.txt")) {
            assert inputStream != null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

                phrases = reader.lines()
                        .filter(line -> !line.isBlank())
                        .toList();
            }
        }
    }

    public String getRandomPhrase() {
        return phrases.get(random.nextInt(phrases.size()));
    }
}
