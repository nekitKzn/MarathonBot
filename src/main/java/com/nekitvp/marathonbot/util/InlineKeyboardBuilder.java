package com.nekitvp.marathonbot.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

/**
 * Билдер для формирования InlineKeyboardMarkup.
 * Позволяет создавать клавиатуру в стиле fluent‑интерфейса, задавая строки кнопок.
 */
public class InlineKeyboardBuilder {

    private final List<List<InlineKeyboardButton>> rows = new ArrayList<>();

    /**
     * Добавляет строку кнопок.
     *
     * @param buttons кнопки, входящие в строку.
     * @return этот же объект InlineKeyboardBuilder для цепочки вызовов.
     */
    public InlineKeyboardBuilder row(InlineKeyboardButton... buttons) {
        rows.add(Arrays.asList(buttons));
        return this;
    }

    /**
     * Строит итоговую клавиатуру.
     *
     * @return объект InlineKeyboardMarkup, содержащий заданные строки кнопок.
     */
    public InlineKeyboardMarkup build() {
        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }
}
