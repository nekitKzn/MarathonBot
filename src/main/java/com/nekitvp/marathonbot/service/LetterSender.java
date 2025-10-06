package com.nekitvp.marathonbot.service;

import com.nekitvp.marathonbot.enumBot.StateBot;
import com.nekitvp.marathonbot.event.SendTelegramMessageEvent;
import com.nekitvp.marathonbot.model.GoalEntity;
import com.nekitvp.marathonbot.model.MarathonEntity;
import com.nekitvp.marathonbot.model.MotivationEntity;
import com.nekitvp.marathonbot.model.UserEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.nekitvp.marathonbot.util.MessageTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import static com.nekitvp.marathonbot.util.Constant.FOGOT_MESSAGE_IN_GROUP;
import static com.nekitvp.marathonbot.util.MessageTemplate.*;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Service
@Slf4j
@RequiredArgsConstructor
public class LetterSender {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final ApplicationEventPublisher publisher;
    private final ReportReminderService reportReminderService;

    /* ---------------- base publish API ---------------- */

    private void publish(Long to, MessageTemplate template, StateBot button, Object... args) {
        String text = template.format(args);
        log.info("Send message to {}: {}", to, text);
        publisher.publishEvent(new SendTelegramMessageEvent(this, text, to, button));
    }

    private void publish(Long to, MessageTemplate template, Object... args) {
        publish(to, template, null, args);
    }

    /** Свободный текст (оставляем для полностью произвольных сообщений, например мотивации из БД). */
    public void publishText(Long to, String rawText) {
        log.info("Send text to {}: {}", to, rawText);
        publisher.publishEvent(new SendTelegramMessageEvent(this, rawText, to, null));
    }

    /* ---------------- high-level API ---------------- */

    /** Отправляет отчёт за текущий день. */
    public void sendReport(UserEntity user, List<Pair<String, Boolean>> report) {
        String name = user.getTelegramFirstName();
        LocalDate today = LocalDate.now();
        String time = LocalTime.now().format(TIME_FMT);
        String content = buildReportContent(report);

        publish(user.getMarathon().getGroupId(), MessageTemplate.DAILY_REPORT,
                name, today, time, content);
    }

    /** Отправляет отчёт за вчера. */
    public void sendYesterdayReport(UserEntity user, List<Pair<String, Boolean>> report) {
        String name = user.getTelegramFirstName();
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String content = buildReportContent(report);

        publish(user.getMarathon().getGroupId(), MessageTemplate.YESTERDAY_REPORT,
                name, yesterday, content);
    }

    /** Личное напоминание тем, кто не поставил отчёт (текст даёт сервис фраз). */
    public void sendWhoDidNotSetReport(UserEntity user) {
        var phrase = reportReminderService.getRandomPhrase();
        publish(user.getTelegramId(), REMINDER_REPORT, StateBot.REPORT,
                String.format(phrase, user.getTelegramFirstName()));
    }

    /** Сообщение о провальном отчёте: личное + в группу с перечнем целей. */
    public void sendBadReport(UserEntity user, List<GoalEntity> goals) {
        // личное
        publish(user.getTelegramId(), MessageTemplate.BAD_REPORT_PRIVATE, StateBot.REPORT_YESTERDAY);

        // групповой отчёт
        String name = user.getTelegramFirstName();
        LocalDate date = LocalDateTime.now().minusHours(12).toLocalDate();

        publish(user.getMarathon().getGroupId(), MessageTemplate.BAD_REPORT_GROUP, name, date);
    }

    /** Мотивация (берём текст целиком из БД/админки). */
    public void sendMotivation(MotivationEntity motivation) {
        publishText(motivation.getMarathon().getGroupId(), motivation.getText());
    }

    /** Сводная статистика по марафону. */
    public void sendStatistics(MarathonEntity marathon, Map<String, Pair<Long, Long>> mapUsers) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = marathon.getDateStart();
        LocalDateTime end = marathon.getDateEnd();

        long totalDays = start.until(end, ChronoUnit.DAYS) + 1;
        long currentDay = Math.max(1, start.until(now, ChronoUnit.DAYS) + 1);
        long daysLeft = Math.max(0, totalDays - currentDay);

        // Рейтинг и касса
        var calc = buildPenaltyRatingBlock(mapUsers, marathon.getFreeFailCount());
        String ratingBlock = calc.ratingText();
        String cashString = calc.cashString();

        publish(marathon.getGroupId(), MessageTemplate.STATISTICS,
                currentDay, daysLeft, ratingBlock, marathon.getFreeFailCount(), cashString);
    }

    /** Сообщение в группы о забывших отправить отчёт. */
    public void sendForgotMessageInGroup(Map<Long, List<UserEntity>> map) {
        map.forEach((groupId, users) -> {
            String names = users.stream()
                    .map(UserEntity::getTelegramFirstName)
                    .collect(Collectors.joining("\n"));
            publish(groupId, MessageTemplate.FORGOT_MESSAGE_GROUP, names);
        });
    }

    /* ---------------- helpers ---------------- */

    /** Формирует содержимое отчёта из списка пар "цель - статус". */
    private String buildReportContent(List<Pair<String, Boolean>> report) {
        StringBuilder contentBuilder = new StringBuilder();
        for (Pair<String, Boolean> entry : report) {
            String result = entry.getSecond() ? "✅" : "❌";
            contentBuilder.append(result).append(" - ").append(entry.getFirst()).append("\n");
        }
        return contentBuilder.toString();
    }

    /** Построение рейтинга штрафов и вычисление кассы (текущая и максимальная). */
    private PenaltyCalc buildPenaltyRatingBlock(Map<String, Pair<Long, Long>> mapUsers, int freeFailCount) {
        List<Map.Entry<String, Pair<Long, Long>>> sorted = mapUsers.entrySet().stream()
                .sorted(Comparator.comparingLong(e -> e.getValue().getFirst()))
                .toList();

        long cash = 0;
        long cashMax = 0;

        StringBuilder rating = new StringBuilder();
        for (Map.Entry<String, Pair<Long, Long>> e : sorted) {
            String name = e.getKey();
            long crosses = e.getValue().getFirst();
            long maxCrosses = e.getValue().getSecond();

            if (crosses == maxCrosses) {
                rating.append(String.format("%d : %s%n", crosses, name));
            } else {
                rating.append(String.format("%d (%d): %s%n", crosses, maxCrosses, name));
            }

            if (crosses > freeFailCount) {
                cash += (crosses - freeFailCount) * 100;
            }
            if (maxCrosses > freeFailCount) {
                cashMax += (maxCrosses - freeFailCount) * 100;
            }
        }
        String cashString = (cash == cashMax) ? String.valueOf(cash) : String.format("%d (%d)", cash, cashMax);
        return new PenaltyCalc(rating.toString().trim(), cashString);
    }

    private record PenaltyCalc(String ratingText, String cashString) {}
}
