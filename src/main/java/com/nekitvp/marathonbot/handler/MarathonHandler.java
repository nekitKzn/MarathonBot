package com.nekitvp.marathonbot.handler;

import com.nekitvp.marathonbot.enumBot.StateBot;
import com.nekitvp.marathonbot.model.UserEntity;
import com.nekitvp.marathonbot.service.HistoryService;
import com.nekitvp.marathonbot.service.MarathonService;
import com.nekitvp.marathonbot.service.UserService;
import com.nekitvp.marathonbot.util.InlineKeyboardBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static com.nekitvp.marathonbot.enumBot.StateBot.*;
import static com.nekitvp.marathonbot.util.Constant.NOT_FOUND_MARATHON;
import static com.nekitvp.marathonbot.util.DateTimeUtil.isRestDay;
import static com.nekitvp.marathonbot.util.TelegramUtil.createButtonByState;

@Component
@RequiredArgsConstructor
public class MarathonHandler extends AbstractHandler {

    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm");

    private final UserService userService;
    private final HistoryService historyService;
    private final MarathonService marathonService;

    @Override
    public StateBot getCurrentState() {
        return StateBot.MARATHON;
    }

    @Override
    public Object handle(Update update) {
        var message = getMessage(update);

        // проверка на существование марафона
        if (!userService.userHasAnyMarathon(message.getChatId())) {
            userService.updateUserState(message.getChatId(), START);
            return getDefaultMessage(message, NOT_FOUND_MARATHON, getKeyboardDefault(START));
        }

        var user = userService.getUser(message.getChatId());
        var marathon = user.getMarathon();
        int freeCount = marathon.getFreeFailCount();

        boolean existsReportToday = historyService.checkExistHistoryToday(user.getTelegramId());
        boolean existsReportYesterday = !historyService.checkExistNullHistoryYesterday(user.getTelegramId());

        var failCount = historyService.getCountFailByUserInMarathon(user);
        String failCountString;
        String resultCash;
        LocalDateTime todayTime = LocalDateTime.now();
        LocalDate todayDay = todayTime.toLocalDate();

        boolean restDay = isRestDay(marathon, todayDay);
        boolean restDayYesterday = isRestDay(marathon, todayDay.minusDays(1));
        boolean isBeforeStart = todayTime.isBefore(marathon.getDateStart());
        boolean isAfterEnd = todayTime.isAfter(marathon.getDateEnd());

        if (Objects.equals(failCount.getFirst(), failCount.getSecond())) {
            failCountString = failCount.getFirst().toString();
            long cash = calculateCash(failCount.getFirst(), freeCount, marathon.getPenaltyAmount());
            resultCash = String.valueOf(cash);
        } else {
            failCountString = String.format("%d (%d)", failCount.getFirst(), failCount.getSecond());
            long cash = calculateCash(failCount.getFirst(), freeCount, marathon.getPenaltyAmount());
            long cashMaybe = calculateCash(failCount.getSecond(), freeCount, marathon.getPenaltyAmount());
            resultCash = String.format("%s (%s)", cash, cashMaybe);
        }

        var keyboard = new InlineKeyboardBuilder()
                .row(createButtonByState(GOAL), createButtonByState(FAIL_GOAL));

        String statusText;

        if (isAfterEnd) {
            statusText = "🏁 Марафон окончен! Спасибо за участие! 🏆";
        } else if (isBeforeStart) {
            statusText = "Марафон скоро начнётся! 🏁";
        } else if (restDay) {
            statusText = "Сегодня выходной от марафона 🌿 Отдых без отчёта!";
        } else {
            if (existsReportToday) {
                keyboard.row(createButtonByState(DELETE_REPORT));
            } else {
                keyboard.row(createButtonByState(REPORT));
            }
            if (!existsReportYesterday) {
                keyboard.row(createButtonByState(REPORT_YESTERDAY));
            }

            statusText = getStatusReport(user, existsReportToday, existsReportYesterday, restDayYesterday, todayDay);
        }

        keyboard.row(createButtonByState(START));

        return getDefaultMessage(message, keyboard.build(),
                user.getTelegramFirstName(),
                marathon.getName(),
                marathon.getDateStart().format(DATE_TIME_FMT),
                marathon.getDateEnd().format(DATE_TIME_FMT),
                freeCount,
                failCountString,
                resultCash,
                statusText
        );
    }

    private String getStatusReport(UserEntity user, boolean existsReportToday,
                                   boolean existsReportYesterday, boolean restDayYesterday, LocalDate today) {
        var marathon = user.getMarathon();

        String todayStatus = "Отчет за сегодня: " + (existsReportToday ? "🟢" : "🔴");
        if (today.equals(marathon.getDateStart().toLocalDate()) || restDayYesterday) {
            return todayStatus;
        }

        String yesterdayStatus = "║ Отчет за вчера: " + (existsReportYesterday ? "🟢" : "🔴");
        return todayStatus + "\n" + yesterdayStatus;
    }

    private long calculateCash(long fails, int freeCount, int penaltyAmount) {
        return fails > freeCount ? (fails - freeCount) * penaltyAmount : 0L;
    }
}

