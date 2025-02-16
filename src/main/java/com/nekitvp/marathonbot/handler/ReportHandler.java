package com.nekitvp.marathonbot.handler;

import com.nekitvp.marathonbot.enumBot.StateBot;
import com.nekitvp.marathonbot.model.GoalEntity;
import com.nekitvp.marathonbot.service.GoalService;
import com.nekitvp.marathonbot.service.HistoryService;
import com.nekitvp.marathonbot.service.MarathonService;
import com.nekitvp.marathonbot.service.UserService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.nekitvp.marathonbot.util.Constant.NOT_FOUND_MARATHON;
import static com.nekitvp.marathonbot.util.Constant.NOT_STARTED_MARATHON;
import static com.nekitvp.marathonbot.util.Constant.REPORT_ALREADY_SEND;

@Component
@RequiredArgsConstructor
public class ReportHandler extends AbstractHandler {

    private final GoalService goalService;
    private final UserService userService;
    private final HistoryService historyService;
    private final MarathonService marathonService;

    @Override
    public StateBot getCurrentState() {
        return StateBot.REPORT;
    }

    @Override
    public StateBot getNextState() {
        return StateBot.REPORT_SEND;
    }


    @Override
    public Object handle(Update update) {

        var message = getMessage(update);

        var listGoal = goalService.getGoalByUser(message.getChatId());
        var user = userService.getUser(message.getChatId());

        // проверка на существование марафона
        if (!userService.userHasAnyMarathon(message.getChatId())) {
            userService.updateUserState(message.getChatId(), StateBot.MARATHON);
            return getDefaultMessage(message, NOT_FOUND_MARATHON, getKeyboardDefault(StateBot.MARATHON));
        }

        // проверка на запуск марафона
        if (!marathonService.marathonIsStarted(user.getMarathon())) {
            userService.updateUserState(message.getChatId(), StateBot.MARATHON);
            return getDefaultMessage(message, NOT_STARTED_MARATHON, getKeyboardDefault(StateBot.MARATHON));
        }

        // если отчет уже отправлен
        if (historyService.checkExistHistoryToday(message.getChatId())) {
            userService.updateUserState(message.getChatId(), StateBot.MARATHON);
            var day = LocalDateTime.now().toLocalDate().toString();
            return getDefaultMessage(message, REPORT_ALREADY_SEND, getKeyboardDefault(StateBot.MARATHON), day);
        }

        List<String> options = listGoal.stream()
                .filter(goal -> goal.getPosition() != 0)
                .map(GoalEntity::getName).toList();

        SendPoll poll = new SendPoll();
        poll.setChatId(message.getChatId());
        poll.setQuestion("Выберите те цели которые вы выполнили: ");
        poll.setOptions(options);
        poll.setAllowMultipleAnswers(true);
        poll.setIsAnonymous(false);

        return poll;
    }
}
