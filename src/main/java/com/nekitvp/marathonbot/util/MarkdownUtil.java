package com.nekitvp.marathonbot.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MarkdownUtil {

    /**
     * Эти символы надо экранировать * _ * [ ] ( ) ~ ` > # + - = | { } . !
     */

    public static String escape(Object text) {
        if (text == null) return "";
        return text.toString()
                .replaceAll("([\\\\_\\*\\[\\]\\(\\)~`>#+\\-=|{}.!])", "\\\\$1");
    }
}