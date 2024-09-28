package com.nekitvp.marathonbot.handler;

import com.nekitvp.marathonbot.enumBot.StateBot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.nekitvp.marathonbot.enumBot.StateBot.START;


@Component
@RequiredArgsConstructor
public class AboutHandler implements Handler {


    @Override
    public StateBot getCurrentState() {
        return StateBot.ABOUT_BOT;
    }

    @Override
    public Object handle(Update update) {

        var message = getMessage(update);
        var keyboard = getKeyboardDefault(START);

        return getDefaultMessage(message, keyboard);
    }
}
