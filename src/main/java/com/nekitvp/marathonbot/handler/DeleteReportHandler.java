package com.nekitvp.marathonbot.handler;

import com.nekitvp.marathonbot.enumBot.StateBot;
import com.nekitvp.marathonbot.service.HistoryService;
import com.nekitvp.marathonbot.service.LetterSender;
import com.nekitvp.marathonbot.service.MarathonService;
import com.nekitvp.marathonbot.service.UserService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.nekitvp.marathonbot.util.Constant.NOT_FOUND_MARATHON;
import static com.nekitvp.marathonbot.util.Constant.NOT_STARTED_MARATHON;
import static com.nekitvp.marathonbot.util.Constant.REPORT_ALREADY_SEND;
import static com.nekitvp.marathonbot.util.Constant.REPORT_NOT_SEND;

@Component
@RequiredArgsConstructor
public class DeleteReportHandler extends AbstractHandler {

    private final HistoryService historyService;
    private final UserService userService;
    private final MarathonService marathonService;
    private final LetterSender letterSender;

    @Override
    public StateBot getCurrentState() {
        return StateBot.DELETE_REPORT;
    }

    @Override
    public Object handle(Update update) {

        var message = getMessage(update);

        var user = userService.getUser(message.getChatId());

        // проверка на существование марафона
        if (!userService.userHasAnyMarathon(message.getChatId())) {
            userService.updateUserState(message.getChatId(), StateBot.MARATHON);
            return getDefaultMessage(message, NOT_FOUND_MARATHON, getKeyboardDefault(StateBot.MARATHON));
        }

        // проверка на запуск марафона
        if (!marathonService.marathonIsStarted(user.getMarathon())) {
            userService.updateUserState(message.getChatId(), StateBot.MARATHON);
            return getDefaultMessage(message, NOT_STARTED_MARATHON, getKeyboardDefault(StateBot.MARATHON));
        }

        // если отчет еще не отправлен
        if (!historyService.checkExistHistoryToday(message.getChatId())) {
            userService.updateUserState(message.getChatId(), StateBot.MARATHON);
            return getDefaultMessage(message, REPORT_NOT_SEND, getKeyboardDefault(StateBot.MARATHON));
        }

        historyService.deleteReport(message.getChatId());

        String text = String.format("‼\uFE0F Отчет марафонца '%s' анулирован ‼\uFE0F", user.getTelegramFirstName());
        var groupId = user.getMarathon().getGroupId();
        letterSender.publishText(groupId, text);

        userService.updateUserState(message.getChatId(), StateBot.START);
        return getDefaultMessage(message, getKeyboardDefault(StateBot.START));
    }
}
