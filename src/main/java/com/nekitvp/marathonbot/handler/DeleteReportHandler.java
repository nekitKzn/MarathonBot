package com.nekitvp.marathonbot.handler;

import com.nekitvp.marathonbot.enumBot.StateBot;
import com.nekitvp.marathonbot.service.HistoryService;
import com.nekitvp.marathonbot.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class DeleteReportHandler implements Handler {

    private final HistoryService historyService;
    private final UserService userService;

    @Override
    public StateBot getCurrentState() {
        return StateBot.DELETE_REPORT;
    }

    @Override
    public Object handle(Update update) {
        var message = getMessage(update);

        historyService.deleteReport(message.getChatId());
        userService.updateUserState(message.getChatId(), StateBot.START);
        return getDefaultMessage(message, getKeyboardDefault(StateBot.START));
    }
}
