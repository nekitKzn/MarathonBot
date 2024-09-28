package com.nekitvp.marathonbot.util;

import com.nekitvp.marathonbot.enumBot.FunctionBot;
import com.nekitvp.marathonbot.enumBot.StateBot;
import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;


@UtilityClass
public class TelegramUtil {


    public static InlineKeyboardButton createButtonByState(StateBot state) {
        return InlineKeyboardButton.builder()
                .text(state.getButtonInThisState())
                .callbackData(state.name())
                .build();
    }

    public static InlineKeyboardButton createButtonByFunction(FunctionBot functionBot) {
        return InlineKeyboardButton.builder()
                .text(functionBot.getButtonForThisFunction())
                .callbackData(functionBot.name())
                .build();
    }

    public static InlineKeyboardButton button(String text, Long callback) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(Long.toString(callback))
                .build();
    }

    public static InlineKeyboardButton createButtonWithUrl(String text, String command, String url) {
        return InlineKeyboardButton.builder()
                .text(text)
                .url(url)
                .callbackData(command)
                .build();
    }

    public static String getNumberSpase(Long number) {
        if (number == 0) {
            return " ".repeat(8);
        } else {
            return " ".repeat(10 - ((int) Math.log10(number) + 1) * 2);
        }
    }
}
