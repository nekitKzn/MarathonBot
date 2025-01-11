package com.nekitvp.marathonbot.sheduller;

import com.nekitvp.marathonbot.service.HistoryService;
import com.nekitvp.marathonbot.service.MotivationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyTaskScheduler {

    private final HistoryService historyService;
    private final MotivationService motivationService;

    /**
     * Метод будет запускаться каждый день в 10 вечера и отпрввлять напоминание тем, кто не отправил отчет
     */
    @Scheduled(cron = "0 0 22 * * ?", zone = "Europe/Moscow")
    public void runTaskEveryDayAt10PM() {
        log.info("Scheduler Notification started...");
        historyService.sendWhoDidNotSetReport();
    }

    /**
     * Метод будет запускаться каждый день в 23:50 вечера и отпрввлять напоминание тем, кто не отправил отчет
     */
    @Scheduled(cron = "0 50 23 * * ?", zone = "Europe/Moscow")
    public void runTaskEveryDayAt23PM() {
        log.info("Scheduler Notification started...");
        historyService.sendWhoDidNotSetReport();
    }

    /**
     * Метод будет запускаться каждый день в 00:00 и отправляет отчеты за тех, кто это не сделал
     */
    @Scheduled(cron = "0 0 0 * * ?", zone = "Europe/Moscow")
    public void runTaskEveryDayAt00() {
        log.info("Scheduler Reports started...");
        historyService.createHistoryWhoFogot();
    }

    /**
     * Метод будет запускаться в 9 утра для мотивации
     */
    @Scheduled(cron = "0 0 9 * * ?", zone = "Europe/Moscow")
    public void motivation9() {
        log.info("Scheduler Motivation started...");
        motivationService.sendRandomMotivation();
    }

    /**
     * Метод будет запускаться в 14 дня для мотивации
     */
    @Scheduled(cron = "0 0 14 * * ?", zone = "Europe/Moscow")
    public void motivation14() {
        log.info("Scheduler Motivation started...");
        motivationService.sendRandomMotivation();
    }

    /**
     * Метод будет запускаться в 9 вечера для мотивации
     */
    @Scheduled(cron = "0 0 21 * * ?", zone = "Europe/Moscow")
    public void motivation21() {
        log.info("Scheduler Motivation started...");
        motivationService.sendRandomMotivation();
    }
}
