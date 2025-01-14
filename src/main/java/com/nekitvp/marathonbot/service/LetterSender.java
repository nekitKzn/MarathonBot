package com.nekitvp.marathonbot.service;

import com.nekitvp.marathonbot.enumBot.StateBot;
import com.nekitvp.marathonbot.event.SendTelegramMessageEvent;
import com.nekitvp.marathonbot.model.GoalEntity;
import com.nekitvp.marathonbot.model.MarathonEntity;
import com.nekitvp.marathonbot.model.MotivationEntity;
import com.nekitvp.marathonbot.model.UserEntity;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import static com.nekitvp.marathonbot.util.TelegramUtil.createButtonByState;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Service
@Slf4j
@RequiredArgsConstructor
public class LetterSender {

    private final ApplicationEventPublisher publisher;
    private final UserMarathonService userMarathonService;
    private final ReportReminderService reportReminderService;

    private void publish(Long to, String template, StateBot button, Object... args) {
        String text = isEmpty(args) ? template : String.format(template, args);
        log.info("Send message to {}: {}", to, text);
        publisher.publishEvent(new SendTelegramMessageEvent(this, text, to, button));
    }

    private void publish(Long to, String template, Object... args) {
        publish(to, template, null, args);
    }

    public void publishText(Long to, String template) {
        publish(to, template);
    }

    public void publishInMarathonsByUserId(Long telegramId, String text) {
        List<Long> listTo = userMarathonService.getGroupIdsByTelegramId(telegramId);
        listTo.forEach(to -> publish(to, text));
    }

    public void sendReport(Long telegramId, String name, List<Pair<String, Boolean>> report) {

        List<Long> listTo = userMarathonService.getGroupIdsByTelegramId(telegramId);

        StringBuilder reportBuilder = new StringBuilder();
        var date = LocalDateTime.now();
        reportBuilder.append("Отчет: ").append(name).append("\n");
        reportBuilder.append("Дата: ").append(date.toLocalDate());
        reportBuilder.append(" ").append(date.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                .append("\n\n");

        for (Pair<String, Boolean> entry : report) {
            String result = Boolean.TRUE.equals(entry.getSecond()) ? "✅" : "❌";
            reportBuilder.append(result).append(" - ").append(entry.getFirst()).append("\n");
        }

        String text = reportBuilder.toString();
        listTo.forEach(to -> publish(to, text));
    }

    public void sendYesterdayReport(Long telegramId, String name, List<Pair<String, Boolean>> report) {

        List<Long> listTo = userMarathonService.getGroupIdsByTelegramId(telegramId);

        StringBuilder reportBuilder = new StringBuilder();
        var date = LocalDateTime.now().minusDays(1).toLocalDate();
        reportBuilder.append("Отчет за вчера: ").append(name).append("\n");
        reportBuilder.append("Дата: ").append(date).append("\n\n");

        for (Pair<String, Boolean> entry : report) {
            String result = Boolean.TRUE.equals(entry.getSecond()) ? "✅" : "❌";
            reportBuilder.append(result).append(" - ").append(entry.getFirst()).append("\n");
        }

        String text = reportBuilder.toString();
        listTo.forEach(to -> publish(to, text));
    }

    public void sendWhoDidNotSetReport(UserEntity user) {
        var phrase = reportReminderService.getRandomPhrase();
        publish(user.getTelegramId(), phrase,  StateBot.REPORT, user.getTelegramFirstName());
    }

    public void sendBadReport(UserEntity user, List<GoalEntity> goals) {

        List<Long> listTo = userMarathonService.getGroupIdsByTelegramId(user.getTelegramId());

        StringBuilder reportBuilder = new StringBuilder();
        var date = LocalDateTime.now().minusHours(12);
        reportBuilder.append("Отчет: ").append(user.getTelegramFirstName()).append("\n");
        reportBuilder.append("Дата: ").append(date.toLocalDate()).append("\n\n");

        reportBuilder.append("Увы, марафонец не справился! \uD83E\uDD72").append("\n\n");

        var list = goals.stream()
                .filter(goal -> goal.getPosition() != 5)
                .toList();

        for (GoalEntity goal : list) {
            reportBuilder.append("❓").append(" - ").append(goal.getName()).append("\n");
        }

        reportBuilder.append("❌").append(" - ").append("Отчет");

        String text = reportBuilder.toString();

        publish(user.getTelegramId(), "Эхх...\uD83E\uDD72\uD83E\uDD72, я в тебя верил))", StateBot.REPORT_YESTERDAY);

        listTo.forEach(id -> publish(id, text));
    }

    public void sendMotivation(MotivationEntity motivation) {
        publishText(motivation.getMarathon().getGroupId(), motivation.getText());
    }

    public void sendStatistics(MarathonEntity marathon, Map<String, Pair<Long, Long>> mapUsers) {

        StringBuilder report = new StringBuilder();
        report.append("Доброе утро, участники марафона! ☀️\n\n");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = marathon.getDateStart();
        LocalDateTime end = marathon.getDateEnd();

        long totalDays = start.until(end, ChronoUnit.DAYS) + 1;
        long currentDay = start.until(now, ChronoUnit.DAYS) + 1;
        long daysLeft = totalDays - currentDay;

        report.append(String.format("Сегодня %d-й день марафона. Осталось %d дней. ⏳\n\n", currentDay, daysLeft));

        report.append("Рейтинг участников по количеству штрафов:\n");
        report.append("---------------\n");

        List<Map.Entry<String, Pair<Long, Long>>> sortedUsers = mapUsers.entrySet()
                .stream()
                .sorted((a, b) -> {
                    Pair<Long, Long> pairA = a.getValue();
                    Pair<Long, Long> pairB = b.getValue();
                    return Long.compare(pairA.getFirst(), pairB.getFirst());
                })
                .toList();

        for (Map.Entry<String, Pair<Long, Long>> entry : sortedUsers) {
            String name = entry.getKey();
            Long crosses = entry.getValue().getFirst();
            Long maxCrosses = entry.getValue().getSecond();

            if (Objects.equals(crosses, maxCrosses)) {
                report.append(String.format("%d : %s \n", crosses, name));
            } else {
                report.append(String.format("%d (%d): %s \n", crosses, maxCrosses, name));
            }
        }

        report.append("---------------\n");
        report.append("Продуктивного дня! \uD83D\uDCAA \n");

        publishText(marathon.getGroupId(), report.toString());
    }
}
