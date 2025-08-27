package com.nekitvp.marathonbot.handler.admin;


import com.nekitvp.marathonbot.enumBot.FunctionBot;
import com.nekitvp.marathonbot.enumBot.StateBot;
import com.nekitvp.marathonbot.handler.AbstractHandler;
import com.nekitvp.marathonbot.model.UserEntity;
import com.nekitvp.marathonbot.service.UserService;
import com.nekitvp.marathonbot.util.InlineKeyboardBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.nekitvp.marathonbot.enumBot.StateBot.ADMIN_MAIN;
import static com.nekitvp.marathonbot.util.TelegramUtil.*;


@Component
@RequiredArgsConstructor
public class AdminListUserUpdateCountHandler extends AbstractHandler {

    private final UserService userService;

    @Override
    public StateBot getCurrentState() {
        return StateBot.ADMIN_LIST_USERS_UPDATE_COUNT;
    }

    @Override
    public Object handle(Update update) {
        var message = getMessage(update);

        var keyboard = new InlineKeyboardBuilder()
                .row(
                        createButtonByFunction(FunctionBot.RESET_COUNT),
                        createButtonByState(ADMIN_MAIN)
                )
                .build();

        var users = userService.getAllUsers();

        String text = users.stream()
                .sorted(Comparator.comparingLong(UserEntity::getCountChangeState).reversed())
                .map(user -> String.format("%s %s| %s%s",
                        user.getCountChangeState(),
                        getNumberSpase(user.getCountChangeState()),
                        Objects.isNull(user.getTelegramUserName()) ? "" : "@" + user.getTelegramUserName() + " ",
                        user.getTelegramFirstName()))
                .collect(Collectors.joining("\n"));

        return getDefaultMessage(message, keyboard, text);
    }
}
