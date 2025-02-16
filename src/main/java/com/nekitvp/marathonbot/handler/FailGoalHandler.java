package com.nekitvp.marathonbot.handler;

import com.nekitvp.marathonbot.enumBot.StateBot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class FailGoalHandler extends AbstractHandler {

    @Override
    public StateBot getCurrentState() {
        return StateBot.FAIL_GOAL;
    }

    @Override
    public Object handle(Update update) {

        var message = getMessage(update);

        return getDefaultMessage(message, getKeyboardDefault(StateBot.MARATHON));
    }
}
