package com.nekitvp.marathonbot.handler;

import com.nekitvp.marathonbot.enumBot.StateBot;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import static com.nekitvp.marathonbot.util.TelegramUtil.createButtonByState;

@Component
@RequiredArgsConstructor
public class RulesHandler implements Handler{

    @Override
    public StateBot getCurrentState() {
        return StateBot.RULES;
    }

    @Override
    public Object handle(Message message) {
        var replyKeyboard = InlineKeyboardMarkup.builder()
                .keyboard(List.of(
                                List.of(createButtonByState(StateBot.START))
                        )
                ).build();

        return getDefaultMessage(message, replyKeyboard);
    }
}
