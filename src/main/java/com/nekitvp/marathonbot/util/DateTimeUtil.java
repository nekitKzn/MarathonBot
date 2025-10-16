package com.nekitvp.marathonbot.util;

import com.nekitvp.marathonbot.model.MarathonEntity;
import lombok.experimental.UtilityClass;
import org.springframework.data.util.Pair;

import java.time.*;
import java.time.format.DateTimeFormatter;

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

    /**
     * Возвращает диапазон для позавчерашнего дня.
     *
     * @return пара (начало позавчерашнего дня, конец позавчерашнего дня)
     */
    public static Pair<LocalDateTime, LocalDateTime> getDayBeforeYesterdayRange() {
        return getDayRange(LocalDate.now().minusDays(2));
    }

    /**
     * Проверка выходного дня
     */
    public static boolean isRestDay(MarathonEntity marathon, LocalDate localDate) {
        DayOfWeek day = localDate.getDayOfWeek();
        return (day == DayOfWeek.SATURDAY && Boolean.TRUE.equals(marathon.getRestOnSaturday()))
                || (day == DayOfWeek.SUNDAY && Boolean.TRUE.equals(marathon.getRestOnSunday()));
    }
}
