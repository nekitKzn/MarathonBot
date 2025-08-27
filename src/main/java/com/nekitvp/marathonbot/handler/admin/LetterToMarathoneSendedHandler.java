package com.nekitvp.marathonbot.handler.admin;

import com.nekitvp.marathonbot.enumBot.StateBot;
import com.nekitvp.marathonbot.handler.AbstractHandler;
import com.nekitvp.marathonbot.service.LetterSender;
import com.nekitvp.marathonbot.service.MarathonService;
import com.nekitvp.marathonbot.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class LetterToMarathoneSendedHandler extends AbstractHandler {

    private final MarathonService marathonService;

    private final UserService userService;
    private final LetterSender letterSender;

    @Override
    public StateBot getCurrentState() {
        return StateBot.LETTER_TO_MARATHON_SANDED;
    }

    @Override
    public Object handle(Update update) {

        var message = getMessage(update);

        var marathonId = marathonService.getIdSelectMarathon();

        letterSender.publishText(marathonId, message.getText());
        userService.updateUserState(message.getChatId(), StateBot.ADMIN_MAIN);

        return getDefaultMessage(message, getKeyboardDefault(StateBot.ADMIN_MAIN));
    }
}
