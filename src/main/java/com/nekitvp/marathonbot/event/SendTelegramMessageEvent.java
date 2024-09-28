package com.nekitvp.marathonbot.event;

import org.springframework.context.ApplicationEvent;

public class SendTelegramMessageEvent extends ApplicationEvent {

    private final String text;

    private final Long chatId;

    private final Integer replyMessageId;


    public SendTelegramMessageEvent(Object source, String text, Long chatId, Integer replyMessageId) {
        super(source);
        this.text = text;
        this.chatId = chatId;
        this.replyMessageId = replyMessageId;
    }

    public String getText() {
        return text;
    }

    public Long getChatId() {
        return chatId;
    }

    public Integer getReplyMessageId() {
        return replyMessageId;
    }
}
