package com.nekitvp.marathonbot.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessageTemplate {

    // Ежедневный отчёт (имя, дата, время, блок целей)
    DAILY_REPORT("""
            Отчет: %s
            Дата: %s %s

            %s
            """),

    // Отчёт за вчера (имя, дата, блок целей)
    YESTERDAY_REPORT("""
            Отчет за вчера: %s
            Дата: %s

            %s
            """),

    REMINDER_REPORT("""
            %s
            """),

    // Личное сообщение при провальном отчёте
    BAD_REPORT_PRIVATE("Эхх...😢😢, я в тебя верил))"),

    // Групповое сообщение при провальном отчёте (имя, дата, блок целей ❓, затем ❌ Отчет)
    BAD_REPORT_GROUP("""
            Отчет: %s
            Дата: %s

            Увы, марафонец не справился! 😢

            ❌ - Отчет
            """),

    // Сообщение в группу о забывших отправить отчёт (список имён)
    FORGOT_MESSAGE_GROUP("""
            Эти марафонцы забыли отправить отчёт 😱:
            %s
            """),

    // Итоговая статистика (текущий день, осталось дней, рейтинг-блок, допустимые бесплатные штрафы, строка кассы)
    STATISTICS("""
            Привет, участники марафона! ☀️

            Сегодня %d-й день марафона. 🏃‍♂️📅 Осталось %d дней. ⏳🔥

            🏆 Рейтинг участников по количеству штрафов:
            ---------------
            %s
            ---------------
            ✅ Допустимо максимум %d бесплатных штрафов.

            💰 В кассе %s рублей. 🤑

            Продуктивного дня! 💪⚡🌈
            """);

    private final String text;

    public String format(Object... args) {
        return (args == null || args.length == 0) ? text : String.format(text, args);
    }
}