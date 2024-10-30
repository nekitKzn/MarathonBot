package com.nekitvp.marathonbot.event;

import com.nekitvp.marathonbot.bot.TelegramBot;

import com.nekitvp.marathonbot.enumBot.StateBot;
import com.nekitvp.marathonbot.handler.Handler;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.nekitvp.marathonbot.util.TelegramUtil.createButtonByState;


@Component
@Slf4j
@RequiredArgsConstructor
public class ServiceEventListener {

    private final TelegramBot telegramBot;

    @Async
    @EventListener
    public void handleSendTelegramMessageEvent(SendTelegramMessageEvent event) {
        var send = SendMessage.builder()
                        .text(event.getText())
                        .chatId(event.getChatId())
                        .build();

        if (event.getButton() != null) {
            var replyKeyboard = InlineKeyboardMarkup.builder()
                    .keyboard(List.of(List.of(createButtonByState(event.getButton())))).build();
            send.setReplyMarkup(replyKeyboard);
        }

        try {
            telegramBot.execute(send);
        } catch (TelegramApiException e) {
            log.debug("Ошибка при отправке", e);
        }
    }
}
