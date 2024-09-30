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

    // Метод будет запускаться каждый день в 11 вечера
    @Scheduled(cron = "0 0 22 * * ?", zone = "Europe/Moscow")
    public void runTaskEveryDayAt11PM() {
        log.info("Scheduler Notification started...");
        historyService.sendWhoDidNotSetReport();
    }
}
