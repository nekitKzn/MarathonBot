package com.nekitvp.marathonbot.handler.admin;

import com.nekitvp.marathonbot.enumBot.FunctionBot;
import com.nekitvp.marathonbot.enumBot.StateBot;
import com.nekitvp.marathonbot.handler.AbstractHandler;
import com.nekitvp.marathonbot.util.InlineKeyboardBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.nekitvp.marathonbot.enumBot.StateBot.ADMIN_LIST_USERS_UPDATE_COUNT;
import static com.nekitvp.marathonbot.enumBot.StateBot.LETTER_TO_MARATHON;
import static com.nekitvp.marathonbot.enumBot.StateBot.START;
import static com.nekitvp.marathonbot.util.TelegramUtil.createButtonByFunction;
import static com.nekitvp.marathonbot.util.TelegramUtil.createButtonByState;


@Component
@RequiredArgsConstructor
public class AdminMainHandler extends AbstractHandler {

    @Override
    public StateBot getCurrentState() {
        return StateBot.ADMIN_MAIN;
    }

    @Override
    public Object handle(Update update) {

        var message = getMessage(update);

        var keyboard = new InlineKeyboardBuilder()
                .row(
                        createButtonByState(StateBot.ADMIN_LIST_USERS_ALL_COUNT),
                        createButtonByState(ADMIN_LIST_USERS_UPDATE_COUNT)
                )
                .row(createButtonByFunction(FunctionBot.SEND_RESULT_REPORT))
                .row(createButtonByState(LETTER_TO_MARATHON))
                .row(createButtonByState(START))
                .build();

        return getDefaultMessage(message, keyboard);
    }
}
