package com.nekitvp.marathonbot.handler;

import com.nekitvp.marathonbot.enumBot.StateBot;
import com.nekitvp.marathonbot.service.HistoryService;
import com.nekitvp.marathonbot.util.InlineKeyboardBuilder;
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
public class StartHandler extends AbstractHandler {

    @Override
    public StateBot getCurrentState() {
        return StateBot.START;
    }

    @Override
    public Object handle(Update update) {

        var message = getMessage(update);

        InlineKeyboardBuilder builder = new InlineKeyboardBuilder();

        builder.row(
                createButtonByState(StateBot.ABOUT_BOT),
                createButtonByState(StateBot.RULES)
        ).row(
                createButtonByState(StateBot.MARATHON)
        );

        var replyKeyboard = builder.build();

        return getDefaultMessage(message, replyKeyboard, message.getChat().getFirstName());
    }
}
