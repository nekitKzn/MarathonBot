package com.nekitvp.marathonbot.handler;

import com.nekitvp.marathonbot.enumBot.StateBot;
import com.nekitvp.marathonbot.service.HistoryService;
import com.nekitvp.marathonbot.service.UserService;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.nekitvp.marathonbot.util.Constant.NOT_FOUND_FAIL_GOAL;

@Component
@RequiredArgsConstructor
public class FailGoalHandler extends AbstractHandler {

    private final HistoryService historyService;
    private final UserService userService;

    @Override
    public StateBot getCurrentState() {
        return StateBot.FAIL_GOAL;
    }

    @Override
    public Object handle(Update update) {

        var message = getMessage(update);

        var user = userService.getUser(message.getChatId());
        var failHistory = historyService.getFailByUserInMarathon(user);

        var text = failHistory.stream()
                .map(history -> history.getCreatedAt().toLocalDate().toString() + " - " + history.getGoalName())
                .collect(Collectors.joining("\n"));

        if (failHistory.isEmpty()) {
            return getDefaultMessage(message, NOT_FOUND_FAIL_GOAL, getKeyboardDefault(StateBot.MARATHON));
        }

        return getDefaultMessage(message, getKeyboardDefault(StateBot.MARATHON), failHistory.size(), text);
    }
}
