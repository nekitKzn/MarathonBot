package com.nekitvp.marathonbot.handler;

import com.nekitvp.marathonbot.enumBot.StateBot;
import com.nekitvp.marathonbot.service.HistoryService;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import static com.nekitvp.marathonbot.util.TelegramUtil.createButtonByState;


@Component
@RequiredArgsConstructor
public class StartHandler implements Handler {

    private final HistoryService historyService;

    @Override
    public StateBot getCurrentState() {
        return StateBot.START;
    }

    @Override
    public Object handle(Update update) {

        var message = getMessage(update);

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        keyboard.add(List.of(createButtonByState(StateBot.ABOUT_BOT),
                createButtonByState(StateBot.RULES)));

        if (historyService.checkExistHistoryToday(message.getChat().getId())) {
            keyboard.add(List.of(createButtonByState(StateBot.DELETE_REPORT)));
        } else {
            keyboard.add(List.of(createButtonByState(StateBot.REPORT)));
        }

        if (historyService.checkExistNullHistoryYesterday(message.getChat().getId())) {
            keyboard.add(List.of(createButtonByState(StateBot.REPORT_YESTERDAY)));
        }

        var replyKeyboard = InlineKeyboardMarkup.builder().keyboard(keyboard).build();

        return getDefaultMessage(message, replyKeyboard, message.getChat().getFirstName());
    }
}
