package com.nekitvp.marathonbot.handler;

import com.nekitvp.marathonbot.enumBot.FunctionBot;
import com.nekitvp.marathonbot.enumBot.StateBot;
import com.nekitvp.marathonbot.service.HistoryService;
import com.nekitvp.marathonbot.service.MarathonService;
import com.nekitvp.marathonbot.service.UserService;
import com.nekitvp.marathonbot.util.InlineKeyboardBuilder;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.nekitvp.marathonbot.enumBot.StateBot.ADMIN_LIST_USERS_UPDATE_COUNT;
import static com.nekitvp.marathonbot.enumBot.StateBot.DELETE_REPORT;
import static com.nekitvp.marathonbot.enumBot.StateBot.FAIL_GOAL;
import static com.nekitvp.marathonbot.enumBot.StateBot.GOAL;
import static com.nekitvp.marathonbot.enumBot.StateBot.LETTER_TO_MARATHON;
import static com.nekitvp.marathonbot.enumBot.StateBot.REPORT;
import static com.nekitvp.marathonbot.enumBot.StateBot.REPORT_YESTERDAY;
import static com.nekitvp.marathonbot.enumBot.StateBot.START;
import static com.nekitvp.marathonbot.util.Constant.NOT_FOUND_MARATHON;
import static com.nekitvp.marathonbot.util.TelegramUtil.createButtonByFunction;
import static com.nekitvp.marathonbot.util.TelegramUtil.createButtonByState;

@Component
@RequiredArgsConstructor
public class MarathonHandler extends AbstractHandler {

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
        var freeCount = marathon.getFreeFailCount();

        var failCount = historyService.getCountFailByUserInMarathone(user);
        String failCountString;
        String resultCash;
        if (Objects.equals(failCount.getFirst(), failCount.getSecond())) {
            failCountString = failCount.getFirst().toString();
            long cash = failCount.getFirst() > freeCount ? (100 * (failCount.getFirst() - freeCount)) : 0;
            resultCash = Long.toString(cash);
        } else {
            failCountString = String.format("%d (%d)", failCount.getFirst(), failCount.getSecond());
            Long cash = failCount.getFirst() > freeCount ? (100 * (failCount.getFirst() - freeCount)) : 0;
            Long cashMayBe = failCount.getSecond() > freeCount ? (100 * (failCount.getSecond() - freeCount)) : 0;
            resultCash = String.format("%s (%s)", cash, cashMayBe);
        }

        var keyboard = new InlineKeyboardBuilder()
                .row(
                        createButtonByState(GOAL),
                        createButtonByState(FAIL_GOAL)
                )
                .row(createButtonByState(REPORT))
                .row(createButtonByState(DELETE_REPORT))
                .row(createButtonByState(REPORT_YESTERDAY))
                .row(createButtonByState(START))
                .build();

        return getDefaultMessage(message, keyboard,
                user.getTelegramFirstName(),
                marathon.getName(),
                marathon.getDateStart().format(DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm")),
                marathon.getDateEnd().format(DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm")),
                freeCount,
                failCountString,
                resultCash
        );
    }
}
