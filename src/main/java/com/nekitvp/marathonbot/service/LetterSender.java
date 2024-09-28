package com.nekitvp.marathonbot.service;

import com.nekitvp.marathonbot.event.SendTelegramMessageEvent;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Service
@Slf4j
@RequiredArgsConstructor
public class LetterSender {

    private final ApplicationEventPublisher publisher;

    @Value("${bot.groupChatId}")
    private Long groupChatId;

    public void sendReport(String name, List<Pair<String, Boolean>> report) {

        StringBuilder reportBuilder = new StringBuilder();
        reportBuilder.append("Отчет: ").append(name).append("\n\n");

        for (Pair<String, Boolean> entry : report) {
            String result = Boolean.TRUE.equals(entry.getSecond()) ? "✅" : "❌";
            reportBuilder.append(result).append(" - ").append(entry.getFirst()).append("\n");
        }

        String text = reportBuilder.toString();
        publisher.publishEvent(new SendTelegramMessageEvent(this, text, groupChatId));
    }

    private void publish(Long to, String template, Object... args) {
        String text = isEmpty(args) ? template : String.format(template, args);
        publisher.publishEvent(new SendTelegramMessageEvent(this, text, to));
    }
}
