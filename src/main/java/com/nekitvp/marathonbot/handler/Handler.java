package com.nekitvp.marathonbot.handler;

import com.nekitvp.marathonbot.enumBot.StateBot;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Интерфейс для обработки обновлений Telegram.
 * Каждый обработчик отвечает за определённое состояние бота.
 */
public interface Handler {

    /**
     * Возвращает состояние, за которое отвечает данный обработчик.
     *
     * @return текущее состояние {@link StateBot}.
     */
    StateBot getCurrentState();

    /**
     * Обрабатывает входящее обновление Telegram.
     *
     * @param update объект обновления Telegram.
     * @return объект ответа (например, {@link org.telegram.telegrambots.meta.api.methods.send.SendMessage} или
     *         {@link org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText}).
     */
    Object handle(Update update);

    /**
     * Возвращает следующее состояние после обработки текущего обновления.
     * Реализация по умолчанию возвращает {@code null}, если переход не определён.
     *
     * @return следующее состояние {@link StateBot} или {@code null}.
     */
    default StateBot getNextState() {
        return null;
    }
}
