package com.nekitvp.marathonbot.handler;

import com.nekitvp.marathonbot.enumBot.StateBot;
import com.nekitvp.marathonbot.service.GoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Comparator;

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

        goals.sort(Comparator.comparingInt(g -> g.getPosition() == 0 ?
                Integer.MAX_VALUE : g.getPosition()));

        String[] emojis = { "0️⃣","1️⃣","2️⃣","3️⃣","4️⃣","5️⃣","6️⃣","7️⃣","8️⃣","9️⃣","\uD83D\uDD1F","#\uFE0F⃣" };
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < goals.size(); i++) {
            int idx = i + 1;
            String num = idx < emojis.length ? emojis[idx] : idx + ".";
            sb.append(num).append(" ").append(goals.get(i).getName()).append("\n");
        }
        String goalsBlock = sb.toString();

        return getDefaultMessage(
                message,
                getKeyboardDefault(StateBot.MARATHON),
                goalsBlock
        );
    }
}
