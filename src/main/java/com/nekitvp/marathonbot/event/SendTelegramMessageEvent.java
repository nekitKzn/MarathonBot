package com.nekitvp.marathonbot.event;

import org.springframework.context.ApplicationEvent;

public class SendTelegramMessageEvent extends ApplicationEvent {

    private final String text;

    private final Long chatId;

    public SendTelegramMessageEvent(Object source, String text, Long chatId) {
        super(source);
        this.text = text;
        this.chatId = chatId;
    }

    public String getText() {
        return text;
    }

    public Long getChatId() {
        return chatId;
    }
}
