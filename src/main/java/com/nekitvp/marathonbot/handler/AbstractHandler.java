package com.nekitvp.marathonbot.handler;

import com.nekitvp.marathonbot.enumBot.StateBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.Arrays;
import java.util.List;

import static com.nekitvp.marathonbot.util.TelegramUtil.createButtonByState;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

public abstract class AbstractHandler implements Handler {


    public Object getDefaultMessage(Message message, InlineKeyboardMarkup keyboard, Object... args) {
        String text = isEmpty(args) ? getCurrentState().getMessage() : String.format(getCurrentState().getMessage(), args);
        if (message.hasReplyMarkup()) {
            return EditMessageText.builder()
                    .messageId(message.getMessageId())
                    .chatId(message.getChatId())
                    .text(text)
                    .replyMarkup(keyboard)
                    .build();
        } else {
            return SendMessage.builder()
                    .chatId(message.getChatId())
                    .text(text)
                    .replyMarkup(keyboard)
                    .build();
        }
    }

    public Object getDefaultMessage(Message message, String template, InlineKeyboardMarkup keyboard, Object... args) {
        String text = isEmpty(args) ? template : String.format(template, args);
        if (message.hasReplyMarkup()) {
            return EditMessageText.builder()
                    .messageId(message.getMessageId())
                    .chatId(message.getChatId())
                    .text(text)
                    .replyMarkup(keyboard)
                    .build();
        } else {
            return SendMessage.builder()
                    .chatId(message.getChatId())
                    .text(text)
                    .replyMarkup(keyboard)
                    .build();
        }
    }


    public SendMessage getSimpleMessage(Message message, InlineKeyboardMarkup keyboardMarkup, Object... args) {
        String text = isEmpty(args) ? getCurrentState().getMessage() : String.format(getCurrentState().getMessage(), args);
        return SendMessage.builder()
                .chatId(message.getChatId())
                .text(text)
                .replyMarkup(keyboardMarkup)
                .build();
    }

    public InlineKeyboardMarkup getKeyboardDefault(StateBot... stateBots) {
        return InlineKeyboardMarkup.builder().keyboard(
                        Arrays.stream(stateBots)
                                .map(state -> List.of(createButtonByState(state)))
                                .toList())
                .build();
    }

    public Message getMessage(Update update) {
        return update.hasCallbackQuery() ? (Message) update.getCallbackQuery().getMessage() : update.getMessage();
    }
}

