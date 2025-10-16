package com.nekitvp.marathonbot.integration.yandex.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class YandexGptResponse {
    private List<Map<String, Object>> choices;
}