package com.nekitvp.marathonbot.sheduller;

import com.nekitvp.marathonbot.service.HistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyTaskScheduler {

    private final HistoryService historyService;

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
}
