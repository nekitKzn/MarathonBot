package com.nekitvp.marathonbot.bot;

import com.nekitvp.marathonbot.service.UpdateHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Objects;

import static com.nekitvp.marathonbot.log.Constant.ERROR_WITH_SENDING_MESSAGE;


@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    private final UpdateHandler updateHandler;
    @Value("${bot.token}")
    private String token;
    @Value("${bot.name}")
    private String botName;

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (Objects.nonNull(update.getMyChatMember()) || badMessage(update)) {
            return;
        }

        Object message = updateHandler.handleMessage(update);

        if (Objects.isNull(message)) {
            return;
        }

        try {
            switch (message) {
                case EditMessageText editMessageText -> execute(editMessageText);
                case SendMessage sendMessage -> execute(sendMessage);
                case SendPhoto sendPhoto -> execute(sendPhoto);
                case SendDocument sendDocument -> execute(sendDocument);
                case SendAudio sendAudio -> execute(sendAudio);
                case SendVideo sendVideo -> execute(sendVideo);
                case SendPoll sendPoll -> execute(sendPoll);
                default -> log.info("Непонятный формат сообщения: {}", message);
            }
        } catch (TelegramApiException e) {
            log.info(ERROR_WITH_SENDING_MESSAGE, e);
        }
    }

    private boolean badMessage(Update update) {
        // разрешенные действия будем пропускать
        boolean result = true;
        if (Objects.nonNull(update.getMessage()) && update.getMessage().isUserMessage()) {
            // любое сообщение в личке (текст, фото, документ)
            result = false;
        }
        if (Objects.nonNull(update.getCallbackQuery()) && update.getCallbackQuery().getMessage().isUserMessage()) {
            // любая кнопка в личке
            result = false;
        }
        if (update.hasPollAnswer()) {
            // если пришел опросник, то пропускаем
            result = false;
        }
        return result;
    }
}
