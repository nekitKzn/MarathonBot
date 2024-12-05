package com.nekitvp.marathonbot.handler;

import com.nekitvp.marathonbot.enumBot.StateBot;
import com.nekitvp.marathonbot.model.GoalEntity;
import com.nekitvp.marathonbot.service.GoalService;
import com.nekitvp.marathonbot.service.HistoryService;
import com.nekitvp.marathonbot.service.UserService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class ReportHandler implements Handler {

    private final GoalService goalService;
    private final UserService userService;
    private final HistoryService historyService;

    @Override
    public StateBot getCurrentState() {
        return StateBot.REPORT;
    }

    @Override
    public StateBot getNextState() {
        return StateBot.REPORT_SEND;
    }

    private static final String TEXT_IF_EMPTY = """
            К сожалению вы еще не на дистанции))
            
            Обратитесь к администратору: @nekit_vp""";

    private static final String TEXT_IF_ALREADY_SEND = """
            Вы уже отправили отчет за сегодня (%s)
            
            Жду тебя завтра! ❤️""";

    @Override
    public Object handle(Update update) {

        var message = getMessage(update);

        var listGoal = goalService.getGoalByUser(message.getChatId());

        // если он не играет или целей нету то выводим пустое сообщение
        if (!userService.getUser(message.getChatId()).isPlaying() || listGoal.isEmpty()) {
            userService.updateUserState(message.getChatId(), StateBot.START);
            return SendMessage.builder()
                    .chatId(message.getChatId())
                    .text(TEXT_IF_EMPTY)
                    .replyMarkup(getKeyboardDefault(StateBot.START))
                    .build();
        }

        // если отчет уже отправлен
        if (historyService.checkExistHistoryToday(message.getChatId())) {
            userService.updateUserState(message.getChatId(), StateBot.START);
            var day = LocalDateTime.now().toLocalDate().toString();
            return SendMessage.builder()
                    .chatId(message.getChatId())
                    .text(String.format(TEXT_IF_ALREADY_SEND, day))
                    .replyMarkup(getKeyboardDefault(StateBot.START))
                    .build();
        }
        
        List<String> options = listGoal.stream()
                .filter(goal -> goal.getPosition() != 5)
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
