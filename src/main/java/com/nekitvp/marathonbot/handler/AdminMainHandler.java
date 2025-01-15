package com.nekitvp.marathonbot.handler;

import com.nekitvp.marathonbot.enumBot.FunctionBot;
import com.nekitvp.marathonbot.enumBot.StateBot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

import static com.nekitvp.marathonbot.enumBot.StateBot.ADMIN_LIST_USERS_UPDATE_COUNT;
import static com.nekitvp.marathonbot.enumBot.StateBot.LETTER_TO_MARATHON;
import static com.nekitvp.marathonbot.enumBot.StateBot.START;
import static com.nekitvp.marathonbot.util.TelegramUtil.createButtonByFunction;
import static com.nekitvp.marathonbot.util.TelegramUtil.createButtonByState;


@Component
@RequiredArgsConstructor
public class AdminMainHandler implements Handler {

    @Override
    public StateBot getCurrentState() {
        return StateBot.ADMIN_MAIN;
    }

    @Override
    public Object handle(Update update) {
        var message = getMessage(update);
        var keyboard = InlineKeyboardMarkup.builder()
                .keyboard(
                        List.of(
                                List.of(createButtonByState(StateBot.ADMIN_LIST_USERS_ALL_COUNT),
                                        createButtonByState(ADMIN_LIST_USERS_UPDATE_COUNT)),
                                List.of(createButtonByFunction(FunctionBot.SEND_RESULT_REPORT)),
                                List.of(createButtonByState(LETTER_TO_MARATHON)),
                                List.of(createButtonByState(START))
                        ))
                .build();

        return getDefaultMessage(message, keyboard);
    }
}
