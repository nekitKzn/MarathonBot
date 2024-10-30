package com.nekitvp.marathonbot.handler;

import com.nekitvp.marathonbot.enumBot.StateBot;
import com.nekitvp.marathonbot.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

import static com.nekitvp.marathonbot.util.TelegramUtil.createButtonByState;


@Component
@RequiredArgsConstructor
public class StartHandler implements Handler {

    private final HistoryService historyService;

    @Override
    public StateBot getCurrentState() {
        return StateBot.START;
    }

    @Override
    public Object handle(Update update) {

        var message = getMessage(update);

        var replyKeyboard = InlineKeyboardMarkup.builder()
                .keyboard(List.of(
                                List.of(createButtonByState(StateBot.ABOUT_BOT),
                                        createButtonByState(StateBot.RULES)),
                                List.of(createButtonByState(StateBot.REPORT))
                        )
                ).build();

        if (historyService.checkExistNullHistoryYesterday(message.getChat().getId())) {
            replyKeyboard = InlineKeyboardMarkup.builder()
                    .keyboard(List.of(
                                    List.of(createButtonByState(StateBot.ABOUT_BOT),
                                            createButtonByState(StateBot.RULES)),
                                    List.of(createButtonByState(StateBot.REPORT)),
                                    List.of(createButtonByState(StateBot.REPORT_YESTERDAY))
                            )
                    ).build();
        }

        return getDefaultMessage(message, replyKeyboard, message.getChat().getFirstName());
    }
}
