package com.nekitvp.marathonbot.integration.yandex.client;

import com.nekitvp.marathonbot.integration.yandex.dto.YandexGptRequest;
import com.nekitvp.marathonbot.integration.yandex.dto.YandexGptResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Клиент для обращения к API YandexGPT.
 * Отправляет запрос к endpoint /chat/completions
 * и возвращает десериализованный ответ.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class YandexGptClient {

    private final WebClient yandexWebClient;

    /**
     * Отправка запроса в Yandex GPT и получение ответа.
     *
     * @param request тело запроса (модель, сообщения, параметры)
     * @return объект ответа от API
     */
    public YandexGptResponse sendRequest(YandexGptRequest request) {
        try {
            log.debug("➡️  Отправка запроса в YandexGPT: {}", request);

            YandexGptResponse response = yandexWebClient.post()
                    .uri("/chat/completions")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(YandexGptResponse.class)
                    .block();

            log.debug("✅ Ответ от YandexGPT: {}", response);
            return response;

        } catch (WebClientResponseException e) {
            log.error("❌ Ошибка при обращении к YandexGPT API: {} | Body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Ошибка при вызове YandexGPT: " + e.getResponseBodyAsString(), e);

        } catch (Exception e) {
            log.error("❌ Ошибка при выполнении запроса к YandexGPT", e);
            throw new RuntimeException("Не удалось выполнить запрос к YandexGPT", e);
        }
    }
}