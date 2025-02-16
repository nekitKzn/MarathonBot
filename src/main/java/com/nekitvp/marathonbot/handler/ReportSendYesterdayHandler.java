package com.nekitvp.marathonbot.handler;

import com.nekitvp.marathonbot.enumBot.StateBot;
import com.nekitvp.marathonbot.service.HistoryService;
import com.nekitvp.marathonbot.service.LetterSender;
import com.nekitvp.marathonbot.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class ReportSendYesterdayHandler extends AbstractHandler {

    private final UserService userService;
    private final HistoryService historyService;
    private  final LetterSender letterSender;

    @Override
    public StateBot getCurrentState() {
        return StateBot.REPORT_SEND_YESTERDAY;
    }

    @Override
    public Object handle(Update update) {
        var poll = update.getPollAnswer();
        var telegramId = poll.getUser().getId();

        List<Pair<String, Boolean>> report = historyService.updateHistory(telegramId, poll.getOptionIds());

        var user = userService.getUser(telegramId);
        letterSender.sendYesterdayReport(user, report);

        userService.updateUserState(telegramId, StateBot.START);

        return SendMessage.builder()
                .chatId(telegramId)
                .text(getCurrentState().getMessage())
                .replyMarkup(getKeyboardDefault(StateBot.START))
                .build();
    }
}
