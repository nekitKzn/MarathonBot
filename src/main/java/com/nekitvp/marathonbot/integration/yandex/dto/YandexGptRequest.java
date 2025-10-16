package com.nekitvp.marathonbot.integration.yandex.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class YandexGptRequest {
    private String model;
    private List<ChatMessage> messages;
    private int maxTokens;
    private double temperature;
}