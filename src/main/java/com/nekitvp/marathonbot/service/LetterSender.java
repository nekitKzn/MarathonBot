package com.nekitvp.marathonbot.service;

import com.nekitvp.marathonbot.event.SendTelegramMessageEvent;
import com.nekitvp.marathonbot.model.GoalEntity;
import com.nekitvp.marathonbot.model.UserEntity;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
    private final UserMarathonService userMarathonService;
    private final ReportReminderService reportReminderService;

    private static final Random random = new Random();

    public void sendReport(Long telegramId, String name, List<Pair<String, Boolean>> report) {

        List<Long> listTo = userMarathonService.getGroupIdsByTelegramId(telegramId);

        StringBuilder reportBuilder = getShapka(name);

        for (Pair<String, Boolean> entry : report) {
            String result = Boolean.TRUE.equals(entry.getSecond()) ? "✅" : "❌";
            reportBuilder.append(result).append(" - ").append(entry.getFirst()).append("\n");
        }

        String text = reportBuilder.toString();
        listTo.forEach(to -> publish(to, text));
    }

    private static StringBuilder getShapka(String name) {
        StringBuilder reportBuilder = new StringBuilder();
        var date = LocalDateTime.now();
        reportBuilder.append("Отчет: ").append(name).append("\n");
        reportBuilder.append("Дата: ").append(date.toLocalDate());
        reportBuilder.append(" ").append(date.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                .append("\n\n");
        return reportBuilder;
    }

    private void publish(Long to, String template, Object... args) {
        String text = isEmpty(args) ? template : String.format(template, args);
        log.info("Send message to {}: {}", to, text);
        publisher.publishEvent(new SendTelegramMessageEvent(this, text, to));
    }

    public void sendWhoDidNotSetReport(UserEntity user) {
        var phrase = reportReminderService.getRandomPhrase();
        publish(user.getTelegramId(), phrase, user.getTelegramFirstName());
    }

    public void sendBadReport(UserEntity user, List<GoalEntity> goals) {

        List<Long> listTo = userMarathonService.getGroupIdsByTelegramId(user.getTelegramId());

        StringBuilder reportBuilder = getShapka(user.getTelegramFirstName());

        reportBuilder.append("Увы, братишка не справился! \uD83E\uDD72").append("\n\n");

        var list = goals.stream()
                .filter(goal -> goal.getPosition() != 5)
                .toList();

        for (GoalEntity goal : list) {
            reportBuilder.append("❓").append(" - ").append(goal.getName()).append("\n");
        }

        reportBuilder.append("❌").append(" - ").append("Отчет");

        String text = reportBuilder.toString();
        listTo.forEach(id -> publish(id, text));
    }
}
