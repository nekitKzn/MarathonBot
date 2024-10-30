package com.nekitvp.marathonbot.service;

import com.nekitvp.marathonbot.enumBot.FunctionBot;
import com.nekitvp.marathonbot.enumBot.StateBot;
import com.nekitvp.marathonbot.handler.Handler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.EnumMap;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class UpdateHandler {

    private static final String ADMIN_COMMAND = "/admin";

    private final UserService userService;
    private final FunctionService functionService;

    private final List<Handler> handlers;
    private final EnumMap<StateBot, Handler> stateToHandlerMap = new EnumMap<>(StateBot.class);

    @PostConstruct
    public void postConstruct() {
        handlers.forEach(handler -> stateToHandlerMap.put(handler.getCurrentState(), handler));
    }

    public Object handleMessage(Update update) {

        if (isMessageWithText(update)) { // пришел текст

            Message message = update.getMessage();
            if (adminCommand(message)) {
                userService.updateUserState(message.getFrom().getId(), StateBot.ADMIN_MAIN);
            }

            StateBot state = userService.getUserState(update.getMessage().getFrom().getId(), message);
            logSendText(message, state);
            return stateToHandlerMap.get(state).handle(update);

        } else if (update.hasCallbackQuery()) { //нажата кнопка

            Long telegramUserId = update.getCallbackQuery().getFrom().getId();
            StateBot state = StateBot.getStateBotByCallBackQuery(update.getCallbackQuery().getData());

            if (Objects.nonNull(state)) { // значит нажата кнопка с состоянием
                logPressedButton(update, state);
                StateBot actualState = userService.updateUserState(telegramUserId, state);
                return stateToHandlerMap.get(actualState).handle(update);

            } else { // значит выбран как либо вариант или функция
                FunctionBot function = FunctionBot.getFunctionBotByCallBackQuery(update.getCallbackQuery().getData());

                if (Objects.nonNull(function)) { // нажата функция
                    function.getFunction().accept(functionService);
                    userService.updateUserState(telegramUserId, function.getStateBot());
                    return stateToHandlerMap.get(function.getStateBot()).handle(update);
                } else { // выбран id
                    StateBot stateBot = userService.findByTelegramId(telegramUserId).getState();
                    StateBot newState = stateToHandlerMap.get(stateBot).getNextState();
                    if (Objects.nonNull(newState)) {
                        userService.updateUserState(telegramUserId, newState);
                        Message message = (Message) update.getCallbackQuery().getMessage();
                        message.setText(update.getCallbackQuery().getData());
                        return stateToHandlerMap.get(newState).handle(update);
                    }
                }
            }

        } else if (isFile(update)) { // отправлен файл
            log.info("Пришел файл от " + update.getMessage().getFrom().getFirstName());
            Long telegramUserId = update.getMessage().getFrom().getId();
            StateBot stateBot = userService.findByTelegramId(telegramUserId).getState();
            StateBot newState = stateToHandlerMap.get(stateBot).getNextState();
            if (Objects.nonNull(newState)) {
                userService.updateUserState(telegramUserId, newState);
                return stateToHandlerMap.get(newState).handle(update);
            }
        } else if (update.hasPollAnswer()) {
            log.info("Пришел опрос от " + update.getPollAnswer().getUser().getFirstName());
            Long telegramUserId = update.getPollAnswer().getUser().getId();
            StateBot stateBot = userService.findByTelegramId(telegramUserId).getState();
            StateBot newState = stateToHandlerMap.get(stateBot).getNextState();
            if (Objects.nonNull(newState)) {
                userService.updateUserState(telegramUserId, newState);
                return stateToHandlerMap.get(newState).handle(update);
            }


        }
        return null;
    }

    private static void logSendText(Message message, StateBot state) {
        log.info("User @{}:{} sent '{}'. State is {}", message.getFrom().getUserName(), message.getFrom().getFirstName(), message.getText(), state.name());
    }

    private static void logPressedButton(Update update, StateBot state) {
        log.info("User @{}:{} pressed button '{}'", update.getCallbackQuery().getFrom().getUserName(),
                update.getCallbackQuery().getFrom().getFirstName(),
                state.getButtonInThisState());
    }

    private boolean adminCommand(Message message) {
        return message.hasEntities() && ADMIN_COMMAND.equals(message.getText())
                && userService.checkIsAdmin(message.getFrom().getId());
    }

    private boolean isMessageWithText(Update update) {
        return !update.hasCallbackQuery() && update.hasMessage() && update.getMessage().hasText();
    }

    private boolean isFile(Update update) {
        return update.hasMessage() &&
                (update.getMessage().hasPhoto() ||
                        update.getMessage().hasDocument() ||
                        update.getMessage().hasVideo() ||
                        update.getMessage().hasAudio());
    }
}
