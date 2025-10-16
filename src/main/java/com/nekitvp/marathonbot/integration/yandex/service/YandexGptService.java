package com.nekitvp.marathonbot.integration.yandex.service;

import com.nekitvp.marathonbot.integration.yandex.client.YandexGptClient;
import com.nekitvp.marathonbot.integration.yandex.dto.ChatMessage;
import com.nekitvp.marathonbot.integration.yandex.dto.YandexGptRequest;
import com.nekitvp.marathonbot.integration.yandex.promt.YandexPromptType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class YandexGptService {

    private final YandexGptClient client;

    @Value("${yandex.folder-id}")
    private String folderId;

    @Value("${yandex.model}")
    private String model;

    @Value("${yandex.temperature}")
    private double temperature;

    @Value("${yandex.max-tokens}")
    private int maxTokens;

    public String ask(String data, YandexPromptType type) {
        String modelUri = "gpt://" + folderId + "/" + model;

        var request = new YandexGptRequest(
                modelUri,
                List.of(
                        new ChatMessage("system", type.getSystemPrompt()),
                        new ChatMessage("user", data)
                ),
                maxTokens,
                temperature
        );

        var response = client.sendRequest(request);

        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            return "⚠️ Модель не ответила.";
        }

        Map<String, Object> message = (Map<String, Object>) response.getChoices().get(0).get("message");
        return (String) message.get("content");
    }
}

