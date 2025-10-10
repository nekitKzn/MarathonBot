package com.nekitvp.marathonbot.service;

import com.nekitvp.marathonbot.enumBot.StateBot;
import com.nekitvp.marathonbot.event.SendTelegramMessageEvent;
import com.nekitvp.marathonbot.model.*;
import com.nekitvp.marathonbot.util.MessageTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.nekitvp.marathonbot.util.MarkdownUtil.escape;
import static com.nekitvp.marathonbot.util.MessageTemplate.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class LetterSender {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final ApplicationEventPublisher publisher;
    private final TemplatePhraseService templatePhraseService;

    /* ---------------- base publish API ---------------- */

    private void publish(Long to, MessageTemplate template, StateBot button, Object... args) {
        String text = template.format(args);
        log.info("Send message to {}:\n {}", to, text);
        publisher.publishEvent(new SendTelegramMessageEvent(this, text, to, button));
    }

    public void publish(Long to, MessageTemplate template, Object... args) {
        publish(to, template, null, args);
    }

    public void publishEscape(Long to, MessageTemplate template, Object... args) {
        Object[] escaped = Arrays.stream(args)
                .map(arg -> arg == null ? "" : escape(arg.toString()))
                .toArray();

        publish(to, template, null, escaped);
    }

    public void publishTextEscape(Long to, String rawText) {
        publish(to, DEFAULT_TEMPLATE, null, escape(rawText));
    }

    public void publish(Long to, String rawText) {
        publish(to, DEFAULT_TEMPLATE, null, rawText);
    }

    /* ---------------- high-level API ---------------- */

    /** Отправляет отчёт за текущий день. */
    public void sendReport(UserEntity user, List<Pair<String, Boolean>> report) {
        String name = user.getTelegramFirstName();
        LocalDate today = LocalDate.now();
        String time = LocalTime.now().format(TIME_FMT);
        String content = buildReportContent(report);

        publish(user.getMarathon().getGroupId(), MessageTemplate.DAILY_REPORT,
                escape(name), escape(today), escape(time), content);
    }

    /** Отправляет отчёт за вчера. */
    public void sendYesterdayReport(UserEntity user, List<Pair<String, Boolean>> report) {
        String name = user.getTelegramFirstName();
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String content = buildReportContent(report);

        publish(user.getMarathon().getGroupId(), MessageTemplate.YESTERDAY_REPORT,
                escape(name), escape(yesterday), content);
    }

    /** Личное напоминание тем, кто не поставил отчёт (текст даёт сервис фраз). */
    public void notifyUsersWithoutTodayReport(UserEntity user) {
        var phrase = templatePhraseService.getRandomMissedToday();
        var text = escape(String.format(phrase, user.getTelegramFirstName()));
        publish(user.getTelegramId(), DEFAULT_TEMPLATE, StateBot.REPORT, text);
    }

    /**
     * Личное напоминание тем, кто не поставил отчёт вчера (текст даёт сервис фраз).
     */
    public void notifyUsersWithoutYesterdayReport(UserEntity user) {
        var phrase = templatePhraseService.getRandomMissedYesterday();
        var text = escape(String.format(phrase, user.getTelegramFirstName()));
        publish(user.getTelegramId(), DEFAULT_TEMPLATE, StateBot.REPORT_YESTERDAY, text);
    }

    /**
     * Сообщение о провальном отчёте: личное + в группу
     */
    public void sendBadReport(UserEntity user) {
        publish(user.getTelegramId(), MessageTemplate.BAD_REPORT_ONE_DAY_PRIVATE, StateBot.REPORT_YESTERDAY);

        String name = user.getTelegramFirstName();
        LocalDate date = LocalDateTime.now().minusDays(1).toLocalDate();

        publishEscape(user.getMarathon().getGroupId(), MessageTemplate.BAD_REPORT_ONE_DAY_GROUP, name, date);
    }

    /**
     * Сообщение о провальном отчёте: личное + в группу
     */
    public void sendBadReportFinal(UserEntity user, List<GoalEntity> goals) {
        publish(user.getTelegramId(), BAD_REPORT_FINAL_DAY_PRIVATE);

        String name = user.getTelegramFirstName();
        LocalDate date = user.getMarathon().getDateEnd().toLocalDate();

        publishEscape(user.getMarathon().getGroupId(), BAD_REPORT_FINAL_DAY_GROUP, name, date, goals.size());
    }

    /** Мотивация (берём текст целиком из БД/админки). */
    public void sendMotivation(MotivationEntity motivation) {
        publishTextEscape(motivation.getMarathon().getGroupId(), motivation.getText());
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
        var calc = buildPenaltyRatingBlock(mapUsers, marathon);
        String ratingBlock = calc.ratingText();
        String cashString = calc.cashString();

        publish(marathon.getGroupId(), MessageTemplate.STATISTICS,
                currentDay, daysLeft, ratingBlock, marathon.getFreeFailCount(), cashString);
    }

    /**
     * Сообщение в группы о забывших отправить отчёт
     */
    public void notifyGroupWithoutReport(Map<Long, List<UserEntity>> map, MessageTemplate template) {
        map.forEach((groupId, users) -> {
            String names = users.stream()
                    .map(user -> escape(user.getTelegramFirstName()))
                    .collect(Collectors.joining("\n• "));
            publish(groupId, template, names);
        });
    }

    /* ---------------- helpers ---------------- */

    /** Формирует содержимое отчёта из списка пар "цель - статус". */
    private String buildReportContent(List<Pair<String, Boolean>> report) {
        StringBuilder contentBuilder = new StringBuilder();
        for (Pair<String, Boolean> entry : report) {
            String result = entry.getSecond() ? "✅" : "❌";
            contentBuilder.append(">").append(result).append(" ").append(escape(entry.getFirst())).append("\n");
        }
        return contentBuilder.toString();
    }

    /** Построение рейтинга штрафов и вычисление кассы (текущая и максимальная). */
    private PenaltyCalc buildPenaltyRatingBlock(Map<String, Pair<Long, Long>> mapUsers, MarathonEntity marathon) {
        List<Map.Entry<String, Pair<Long, Long>>> sorted = mapUsers.entrySet().stream()
                .sorted(Comparator.comparingLong(e -> e.getValue().getFirst()))
                .toList();

        long cash = 0;
        long cashMax = 0;
        var freeFailCount = marathon.getFreeFailCount();
        var penaltyAmount = marathon.getPenaltyAmount();

        StringBuilder rating = new StringBuilder();
        for (Map.Entry<String, Pair<Long, Long>> e : sorted) {
            String name = escape(e.getKey());
            long crosses = e.getValue().getFirst();
            long maxCrosses = e.getValue().getSecond();

            if (crosses == maxCrosses) {
                rating.append(String.format("%d : %s%n", crosses, name));
            } else {
                rating.append(String.format("%d \\(%d\\): %s%n", crosses, maxCrosses, name));
            }

            if (crosses > freeFailCount) {
                cash += (crosses - freeFailCount) * penaltyAmount;
            }
            if (maxCrosses > freeFailCount) {
                cashMax += (maxCrosses - freeFailCount) * penaltyAmount;
            }
        }
        String cashString = (cash == cashMax) ? String.valueOf(cash) : String.format("%d \\(%d\\)", cash, cashMax);
        return new PenaltyCalc(rating.toString().trim(), cashString);
    }

    public void sendRedReportWarning(UserEntity user, List<HistoryEntity> histories) {
        var marathon = user.getMarathon();
        var userName = escape(user.getTelegramFirstName());
        var dayBeforeYesterday = escape(LocalDate.now().minusDays(2));
        String failedGoalsList = histories.stream()
                .map(h -> ">❌ " + escape(h.getGoal().getName()))
                .collect(Collectors.joining("\n"));
        publish(marathon.getGroupId(), BAD_REPORT_TWO_DAYS_GROUP, userName, dayBeforeYesterday, failedGoalsList);
        publish(user.getTelegramId(), BAD_REPORT_TWO_DAYS_PRIVATE);
    }

    private record PenaltyCalc(String ratingText, String cashString) {}
}
