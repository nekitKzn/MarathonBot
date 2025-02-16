package com.nekitvp.marathonbot.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.springframework.data.util.Pair;

@UtilityClass
public class DateTimeUtil {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy")
            .withZone(ZoneId.systemDefault());

    public static String getDate(Instant createAt) {
        return formatter.format(createAt);
    }

    /**
     * Возвращает время начала дня для указанной даты.
     *
     * @param date заданная дата
     * @return начало дня (00:00) для date
     */
    private static LocalDateTime getStartOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    /**
     * Возвращает время конца дня для указанной даты.
     *
     * @param date заданная дата
     * @return время конца дня (23:59:59.999...) для date
     */
    private static LocalDateTime getEndOfDay(LocalDate date) {
        return date.atTime(LocalTime.MAX);
    }

    /**
     * Возвращает пару границ (начало и конец) для указанного дня.
     *
     * @param date заданная дата
     * @return пара (начало дня, конец дня)
     */
    private static Pair<LocalDateTime, LocalDateTime> getDayRange(LocalDate date) {
        return Pair.of(getStartOfDay(date), getEndOfDay(date));
    }

    /**
     * Возвращает диапазон для текущего дня.
     *
     * @return пара (начало сегодняшнего дня, конец сегодняшнего дня)
     */
    public static Pair<LocalDateTime, LocalDateTime> getTodayRange() {
        return getDayRange(LocalDate.now());
    }

    /**
     * Возвращает диапазон для вчерашнего дня.
     *
     * @return пара (начало вчерашнего дня, конец вчерашнего дня)
     */
    public static Pair<LocalDateTime, LocalDateTime> getYesterdayRange() {
        return getDayRange(LocalDate.now().minusDays(1));
    }
}
