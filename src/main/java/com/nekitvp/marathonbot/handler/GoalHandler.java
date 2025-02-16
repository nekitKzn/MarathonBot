package com.nekitvp.marathonbot.handler;

import com.nekitvp.marathonbot.enumBot.StateBot;
import com.nekitvp.marathonbot.service.GoalService;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class GoalHandler extends AbstractHandler {

    private final GoalService goalService;

    @Override
    public StateBot getCurrentState() {
        return StateBot.GOAL;
    }

    @Override
    public Object handle(Update update) {
        var message = getMessage(update);

        var goals = goalService.getGoalByUser(message.getChatId());
        var result = goals.stream()
                .map(goal -> goal.getPosition() + " - " + goal.getName())
                .collect(Collectors.joining("\n"));

        return getDefaultMessage(message, getKeyboardDefault(StateBot.MARATHON), result);
    }
}
