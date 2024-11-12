package com.nekitvp.marathonbot.handler;

import com.nekitvp.marathonbot.enumBot.StateBot;
import com.nekitvp.marathonbot.service.MarathonService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import static com.nekitvp.marathonbot.enumBot.StateBot.LETTER_TO_MARATHON_SELECT;
import static com.nekitvp.marathonbot.util.TelegramUtil.button;
import static com.nekitvp.marathonbot.util.TelegramUtil.createButtonByState;

@Component
@RequiredArgsConstructor
public class LetterToMarathonHandler implements Handler {

    private final MarathonService marathonService;

    private static final String FIND_MARATHON = "Найденные марафоны";

    private static final String NOT_FOUND_MARATHON = "Чатов с марафоном не найдено";

    @Override
    public StateBot getCurrentState() {
        return StateBot.LETTER_TO_MARATHON;
    }

    @Override
    public StateBot getNextState() {
        return LETTER_TO_MARATHON_SELECT;
    }

    @Override
    public Object handle(Update update) {

        var message = getMessage(update);

        Map<Long, String> list = marathonService.getAllActiveMarathones();

        List<List<InlineKeyboardButton>> lists = new ArrayList<>(list.entrySet().stream()
                .map(entry -> button(entry.getValue(), entry.getKey()))
                .map(List::of).toList());
        lists.add(List.of(createButtonByState(StateBot.ADMIN_MAIN)));
        var replyKeyboard = InlineKeyboardMarkup.builder()
                .keyboard(lists).build();

        if (list.isEmpty()) {
            return getDefaultMessage(message, replyKeyboard, NOT_FOUND_MARATHON);
        } else {
            return getDefaultMessage(message, replyKeyboard, FIND_MARATHON);
        }
    }
}
