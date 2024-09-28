package com.nekitvp.marathonbot.handler;

import com.nekitvp.marathonbot.enumBot.StateBot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

import static com.nekitvp.marathonbot.enumBot.StateBot.START;
import static com.nekitvp.marathonbot.util.TelegramUtil.createButtonByState;


@Component
@RequiredArgsConstructor
public class AboutHandler implements Handler {


    @Override
    public StateBot getCurrentState() {
        return StateBot.ABOUT_BOT;
    }

    @Override
    public Object handle(Message message) {

        var keyboard = InlineKeyboardMarkup.builder()
                .keyboard(List.of(
                        List.of(createButtonByState(START))))
                .build();

        return getDefaultMessage(message, keyboard);
    }
}
