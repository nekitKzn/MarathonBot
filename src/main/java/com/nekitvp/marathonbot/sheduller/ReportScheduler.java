package com.nekitvp.marathonbot.sheduller;

import com.nekitvp.marathonbot.service.GoalService;
import com.nekitvp.marathonbot.service.HistoryService;
import com.nekitvp.marathonbot.service.MarathonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportScheduler {

    private final HistoryService historyService;
    private final MarathonService marathonService;
    private final GoalService goalService;

    /**
     * Напоминание за час до старта (23:00 накануне).
     */
    @Scheduled(cron = "0 0 23 * * ?", zone = "Europe/Moscow")
    public void remindBeforeStart() {
        log.info("=== [MARATHON SCHEDULER] Reminder before start ===");
        marathonService.remindMarathonsStartingInHour();
    }

    /**
     * Старт и завершение марафонов (00:00).
     */
    @Scheduled(cron = "0 0 0 * * ?", zone = "Europe/Moscow")
    public void handleLifecycle() {
        log.info("=== [REPORT SCHEDULER] 00:00 tasks started ===");

        // 1. Сначала обновляем старые незаполненные отчёты (2 дня)
        historyService.updateHistoryWhoForgotTwoDays();

        // 2. Затем создаем отчёты за вчера
        historyService.createHistoryWhoForgotOneday();

        // 3. И уже потом управляем марафонами
        marathonService.startMarathonsIfNeeded();
        marathonService.finishMarathonsIfNeeded();

        log.info("=== [REPORT SCHEDULER] 00:00 tasks completed ===");
    }

    /**
     * Утренний отчёт о результатах (6:00).
     */
    @Scheduled(cron = "0 0 6 * * ?", zone = "Europe/Moscow")
    public void sendDailyStatistics() {
        log.info("=== [REPORT SCHEDULER] Sending daily stats ===");
        marathonService.sendStatistics();
    }

    /**
     * Напоминание о последнем дне (06:01).
     */
    @Scheduled(cron = "0 1 6 * * ?", zone = "Europe/Moscow")
    public void remindLastDay() {
        log.info("=== [MARATHON SCHEDULER] Last day reminder ===");
        marathonService.remindLastDay();
    }

    /**
     * Напоминание о выходном (06:02).
     */
    @Scheduled(cron = "0 2 6 * * ?", zone = "Europe/Moscow")
    public void remindRestDay() {
        log.info("=== [MARATHON SCHEDULER] Rest day reminder ===");
        marathonService.remindRestDay();
    }

    /**
     * Ежевечерный опросник
     */
    @Scheduled(cron = "0 0 18 * * ?", zone = "Europe/Moscow")
    public void eveningQuestion() {
        log.info("=== EveningQuestion ===");
        goalService.sendEveningQuestion();
    }

    /**
     * Ежедневное напоминание участникам, которые не отправили отчёт за сегодня или вчера
     * 22:00 — мягкое напоминание, 23:50 — финальное.
     */
    @Scheduled(cron = "0 0 22 * * ?", zone = "Europe/Moscow")
    public void remindAboutReportsEvening() {
        log.info("=== [REPORT SCHEDULER] Evening reminder ===");
        historyService.notifyUsersWithoutTodayReport();
    }

    @Scheduled(cron = "0 50 23 * * ?", zone = "Europe/Moscow")
    public void remindAboutReportsLate() {
        log.info("=== [REPORT SCHEDULER] Late-night reminder ===");
        historyService.notifyUsersWithoutTodayReport();
    }
}
