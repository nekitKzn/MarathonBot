package com.nekitvp.marathonbot.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constant {

    public static final String NOT_FOUND_MARATHON = """
            🌟 Ой-ой! Кажется, ты ещё не вступил в наш марафонный отряд! 
            
            Не беда — великие приключения уже рядом! Напиши @nekit_vp и узнай, как стать частью команды! 🚀
            """;

    public static final String NOT_STARTED_MARATHON = """
            ⏳ Стоп-старт! Марафон не активен!
            
            Проверь даты 🔥
            """;

    public static final String REPORT_ALREADY_SEND = """
            🚀 Супергерой, ты уже сделал это! Твой отчёт за сегодня (%s) уже принят! 📝✨
            
            Откинься, расслабься и готовься к новым подвигам завтра! 💪😎
            """;

    public static final String TEXT_IF_HAS_REPORT_YESTERDAY = """
            🤫 Псс... Вчера ты уже отправил отчёт, так что можно смело расслабиться — всё идёт по плану!
            """;

    public static final String REPORT_NOT_SEND = """
            ⚠️ Эй, чемпион! Сегодня твой отчёт ещё не отправлен. 
            
            Давай заполним этот пустой экран, чтобы твоя звезда засияла! 📝🔥
            """;


}
