package com.nekitvp.marathonbot.handler;

import com.nekitvp.marathonbot.enumBot.StateBot;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import static com.nekitvp.marathonbot.util.TelegramUtil.createButtonByState;

@Component
@RequiredArgsConstructor
public class RulesHandler extends AbstractHandler {

    @Override
    public StateBot getCurrentState() {
        return StateBot.RULES;
    }

    @Override
    public Object handle(Update update) {

        var message = getMessage(update);
        var replyKeyboard = getKeyboardDefault(StateBot.START);

        return getDefaultMessage(message, replyKeyboard);
    }
}
