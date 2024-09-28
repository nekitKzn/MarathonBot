package com.nekitvp.marathonbot.handler;

import com.nekitvp.marathonbot.enumBot.StateBot;
import com.nekitvp.marathonbot.service.GoalService;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@RequiredArgsConstructor
public class ReportHandler implements Handler {

    private final GoalService goalService;

    @Override
    public StateBot getCurrentState() {
        return StateBot.REPORT;
    }

    @Override
    public Object handle(Message message) {

        List<String> options = List.of("Цель 1", "Цель 2", "Цель 3", "Цель 4", "Цель 5");

        SendPoll poll = new SendPoll();
        poll.setChatId(message.getChatId());
        poll.setQuestion("Выберите те цели которые вы выполнили:");
        poll.setOptions(options);
        poll.setAllowMultipleAnswers(true);
        poll.setIsAnonymous(false);

        return poll;
    }
}
