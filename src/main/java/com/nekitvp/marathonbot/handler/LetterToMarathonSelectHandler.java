package com.nekitvp.marathonbot.handler;

import com.nekitvp.marathonbot.enumBot.StateBot;
import com.nekitvp.marathonbot.service.MarathonService;
import com.nekitvp.marathonbot.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.nekitvp.marathonbot.enumBot.StateBot.LETTER_TO_MARATHON_SELECT;

@Component
@RequiredArgsConstructor
public class LetterToMarathonSelectHandler implements Handler {

    private final MarathonService marathonService;
    private final UserService userService;

    @Override
    public StateBot getCurrentState() {
        return LETTER_TO_MARATHON_SELECT;
    }

    @Override
    public Object handle(Update update) {

        var message = getMessage(update);

        marathonService.selectMarathon(Long.valueOf(message.getText()));
        userService.updateUserState(message.getChatId(), StateBot.LETTER_TO_MARATHON_SANDED);
        return getDefaultMessage(message, null);
    }
}
