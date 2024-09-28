package com.nekitvp.marathonbot.service;

import com.nekitvp.marathonbot.event.SendTelegramMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Service
@Slf4j
@RequiredArgsConstructor
public class LetterSender {

    private final ApplicationEventPublisher publisher;

    @Value("${bot.groupChatId}")
    private Long groupChatId;

    @Value("${bot.replyMessageId}")
    private Integer replyMessageId;

    private final static String NEW_QUESTION_ALL = """
            Поступил вопрос через бота ❓
            Содержание:
                        
            «%s»
            """;


    private void publish(Long to, Integer replyMessageId, String template, Object... args) {
        String text = isEmpty(args) ? template : String.format(template, args);
        publisher.publishEvent(new SendTelegramMessageEvent(this, text, to, replyMessageId));
    }
}
