package com.nekitvp.marathonbot.handler.admin;

import com.nekitvp.marathonbot.enumBot.StateBot;
import com.nekitvp.marathonbot.handler.AbstractHandler;
import com.nekitvp.marathonbot.model.UserEntity;
import com.nekitvp.marathonbot.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.nekitvp.marathonbot.enumBot.StateBot.ADMIN_MAIN;
import static com.nekitvp.marathonbot.util.TelegramUtil.getNumberSpase;


@Component
@RequiredArgsConstructor
public class AdminListUserAllCountHandler extends AbstractHandler {

    private final UserService userService;

    @Override
    public StateBot getCurrentState() {
        return StateBot.ADMIN_LIST_USERS_ALL_COUNT;
    }

    @Override
    public Object handle(Update update) {

        var message = getMessage(update);
        var keyboard = getKeyboardDefault(ADMIN_MAIN);
        var users = userService.getAllUsers();
        String text = users.stream()
                .sorted(Comparator.comparingLong(UserEntity::getCountChangeStateAll).reversed())
                .map(user -> String.format("%s %s| %s%s",
                        user.getCountChangeStateAll(),
                        getNumberSpase(user.getCountChangeStateAll()),
                        Objects.isNull(user.getTelegramUserName()) ? "" : "@" + user.getTelegramUserName() + " ",
                        user.getTelegramFirstName()))
                .collect(Collectors.joining("\n"));
        return getDefaultMessage(message, keyboard, text);
    }
}
