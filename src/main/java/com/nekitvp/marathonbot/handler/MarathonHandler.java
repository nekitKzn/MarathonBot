package com.nekitvp.marathonbot.handler;

import com.nekitvp.marathonbot.enumBot.StateBot;
import com.nekitvp.marathonbot.model.UserEntity;
import com.nekitvp.marathonbot.service.HistoryService;
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
import static com.nekitvp.marathonbot.util.TelegramUtil.createButtonByState;

@Component
@RequiredArgsConstructor
public class MarathonHandler extends AbstractHandler {

    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm");

    private final UserService userService;
    private final HistoryService historyService;

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

        if (Objects.equals(failCount.getFirst(), failCount.getSecond())) {
            failCountString = failCount.getFirst().toString();
            long cash = calculateCash(failCount.getFirst(), freeCount);
            resultCash = String.valueOf(cash);
        } else {
            failCountString = String.format("%d (%d)", failCount.getFirst(), failCount.getSecond());
            long cash = calculateCash(failCount.getFirst(), freeCount);
            long cashMaybe = calculateCash(failCount.getSecond(), freeCount);
            resultCash = String.format("%s (%s)", cash, cashMaybe);
        }

        var keyboard = new InlineKeyboardBuilder()
                .row(createButtonByState(GOAL), createButtonByState(FAIL_GOAL));

        if (existsReportToday) {
            keyboard.row(createButtonByState(DELETE_REPORT));
        } else if (LocalDateTime.now().isAfter(marathon.getDateStart())
                && LocalDateTime.now().isBefore(marathon.getDateEnd())) {
            keyboard.row(createButtonByState(REPORT));
        }

        if (!existsReportYesterday) {
            keyboard.row(createButtonByState(REPORT_YESTERDAY));
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
                getStatusReport(user, existsReportToday, existsReportYesterday)
        );
    }

    private String getStatusReport(UserEntity user, boolean existsReportToday, boolean existsReportYesterday) {
        var marathon = user.getMarathon();
        LocalDate today = LocalDate.now();

        if (today.isAfter(marathon.getDateEnd().toLocalDate())) {
            return "Марафон окончен! 🏆";
        }
        if (today.isBefore(marathon.getDateStart().toLocalDate())) {
            return "Марафон скоро начнется! 🏁";
        }

        String todayStatus = "Отчет за сегодня: " + (existsReportToday ? "🟢" : "🔴");
        if (today.equals(marathon.getDateStart().toLocalDate())) {
            return todayStatus;
        }

        String yesterdayStatus = "║ Отчет за вчера: " + (existsReportYesterday ? "🟢" : "🔴");
        return todayStatus + "\n" + yesterdayStatus;
    }

    private long calculateCash(long fails, int freeCount) {
        return fails > freeCount ? (fails - freeCount) * 100L : 0L;
    }
}

