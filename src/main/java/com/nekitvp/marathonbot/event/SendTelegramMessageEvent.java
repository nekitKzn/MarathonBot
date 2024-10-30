package com.nekitvp.marathonbot.event;

import com.nekitvp.marathonbot.enumBot.StateBot;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.lang.Nullable;

@Getter
public class SendTelegramMessageEvent extends ApplicationEvent {

    private final String text;

    private final Long chatId;

    @Nullable
    private final StateBot button;

    public SendTelegramMessageEvent(Object source, String text, Long chatId, @Nullable StateBot button) {
        super(source);
        this.text = text;
        this.chatId = chatId;
        this.button = button;
    }
}
