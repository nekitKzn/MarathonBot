package com.nekitvp.marathonbot.service;


import com.nekitvp.marathonbot.model.GoalEntity;
import com.nekitvp.marathonbot.model.HistoryEntity;
import com.nekitvp.marathonbot.model.MarathonEntity;
import com.nekitvp.marathonbot.model.UserEntity;
import com.nekitvp.marathonbot.repository.HistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.nekitvp.marathonbot.util.DateTimeUtil.*;
import static com.nekitvp.marathonbot.util.MessageTemplate.NOTIFY_GROUP_WITHOUT_REPORT_TODAY;
import static com.nekitvp.marathonbot.util.MessageTemplate.NOTIFY_GROUP_WITHOUT_REPORT_YESTERDAY;
import static java.time.LocalDateTime.now;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryService {

    private final HistoryRepository historyRepository;
    private final GoalService goalService;
    private final UserService userService;
    private final LetterSender letterSender;

    /**
     * Заполнение отчета за сегодня
     */
    @Transactional
    public List<Pair<String, Boolean>> createHistoryGoal(Long telegramId, List<Integer> optionIds) {

        var goals = goalService.getGoalByUser(telegramId);
        List<Pair<String, Boolean>> result = new ArrayList<>();
        LocalDateTime currentTime = now();

        goals.sort(Comparator.comparingInt(g -> g.getPosition() == 0 ?
                Integer.MAX_VALUE : g.getPosition()));

        for (var goal : goals) {
            boolean completed;
            if (goal.getPosition() == 0) {
                completed = true;
            } else {
                completed = optionIds.contains(goal.getPosition() - 1);
            }
            result.add(Pair.of(goal.getName(), completed));
            HistoryEntity history = createHistoryGoal(goal, completed, currentTime);
            historyRepository.save(history);
        }
        return result;
    }

    /**
     * Заполнение отчета за вчера
     */
    @Transactional
    public List<Pair<String, Boolean>> updateHistory(Long telegramId, List<Integer> optionIds) {

        List<Pair<String, Boolean>> result = new ArrayList<>();

        Pair<LocalDateTime, LocalDateTime> yesterdayRange = getYesterdayRange();

        var histories = historyRepository.findByTelegramIdAndCreatedAtBetween(
                telegramId, yesterdayRange.getFirst(), yesterdayRange.getSecond()
        );

        histories.sort(Comparator.comparingInt(history -> history.getGoal().getPosition() == 0 ?
                Integer.MAX_VALUE : history.getGoal().getPosition()));

        for (var history : histories) {
            var goal = history.getGoal();
            boolean completed = optionIds.contains(goal.getPosition() - 1);
            result.add(Pair.of(goal.getName(), completed));
            history.setDone(completed);
        }
        historyRepository.saveAll(histories);
        return result;
    }

    private HistoryEntity createHistoryGoal(GoalEntity goal, Boolean completed, LocalDateTime time) {
        return HistoryEntity.builder()
                .goal(goal)
                .done(completed)
                .createdAt(time)
                .build();
    }

    /**
     * А есть ли у пользователя отчет за сегодня?
     */
    public boolean checkExistHistoryToday(Long telegramId) {
        Pair<LocalDateTime, LocalDateTime> todayRange = getTodayRange();
        return historyRepository.existsByTelegramIdAndCreatedAtBetween(telegramId, todayRange.getFirst(),
                todayRange.getSecond());
    }

    /**
     * А есть ли у пользователя незаполненный отчет за вчера?
     */
    public boolean checkExistNullHistoryYesterday(Long telegramId) {
        Pair<LocalDateTime, LocalDateTime> yesterdayRange = getYesterdayRange();
        return historyRepository.findByTelegramIdAndCreatedAtBetween(telegramId, yesterdayRange.getFirst(),
                        yesterdayRange.getSecond())
                .stream().anyMatch(history -> history.getDone() == null);
    }

    /**
     * Отправляем напоминание тем, кто не отправил отчет, а также в группу
     */
    @Transactional(readOnly = true)
    public void notifyUsersWithoutTodayReport() {
        Set<UserEntity> userSetForToday = new HashSet<>();
        Set<UserEntity> userSetForYestarday = new HashSet<>();
        LocalDate today = LocalDate.now();

        userService.getUsersWhoHasActiveMarathon().forEach(user -> {
            MarathonEntity marathon = user.getMarathon();
            if (checkExistNullHistoryYesterday(user.getTelegramId())) {
                userSetForYestarday.add(user);
                letterSender.notifyUsersWithoutYesterdayReport(user);
            }
            if (isRestDay(marathon, today)) {
                log.info("Skip reminder for {} — rest day in marathon {}", user.getTelegramFirstName(), marathon.getName());
                return;
            }
            if (!checkExistHistoryToday(user.getTelegramId())) {
                letterSender.notifyUsersWithoutTodayReport(user);
                userSetForToday.add(user);
            }
        });

        Map<Long, List<UserEntity>> mapForToday = userSetForToday.stream()
                .collect(Collectors.groupingBy(user -> user.getMarathon().getGroupId()));
        Map<Long, List<UserEntity>> mapForYesterday = userSetForYestarday.stream()
                .collect(Collectors.groupingBy(user -> user.getMarathon().getGroupId()));

        letterSender.notifyGroupWithoutReport(mapForToday, NOTIFY_GROUP_WITHOUT_REPORT_TODAY);
        letterSender.notifyGroupWithoutReport(mapForYesterday, NOTIFY_GROUP_WITHOUT_REPORT_YESTERDAY);
    }

    /**
     * Создаем отчеты за тех, кто не отправил за день
     */
    @Transactional
    public void createHistoryWhoForgotOneday() {
        Pair<LocalDateTime, LocalDateTime> yesterdayRange = getYesterdayRange();
        var timeReport = yesterdayRange.getFirst().plusHours(12);
        var today = LocalDate.now();

        List<HistoryEntity> historiesToSave = new ArrayList<>();

        userService.getUsersWhoHasActiveMarathon()
                .forEach(user -> {
                    MarathonEntity marathon = user.getMarathon();
                    if (marathon.getDateStart().toLocalDate()
                            .isAfter(today.minusDays(1))) {
                        return;
                    }
                    if (isRestDay(marathon, yesterdayRange.getFirst().toLocalDate())) {
                        log.info("Skip report creation for {} (weekend in marathon {})", user.getTelegramFirstName(), marathon.getName());
                        return;
                    }

                    var reportExists = historyRepository.existsByTelegramIdAndCreatedAtBetween(user.getTelegramId(),
                            yesterdayRange.getFirst(),
                            yesterdayRange.getSecond());

                    if (!reportExists) {
                        var goals = goalService.getGoalByUser(user.getTelegramId());
                        goals.stream()
                                .filter(goal -> goal.getPosition() != 0)
                                .forEach(goal -> {
                                    var history = createHistoryGoal(goal, null, timeReport);
                                    historiesToSave.add(history);
                                });
                        var goalReport = goals.stream()
                                .filter(goal -> goal.getPosition() == 0)
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("Goal Report not found"));
                        var history = createHistoryGoal(goalReport, false, timeReport);
                        historiesToSave.add(history);
                        letterSender.sendBadReport(user);
                    }
                });
        historyRepository.saveAll(historiesToSave);
    }

    /**
     * Через 24 часа после незаполненного отчета (done = null) — проставляем крестики (done = false).
     * Проверяем всех участников, у которых позавчера был активен марафон (даже если сегодня он уже завершен).
     */
    @Transactional
    public void updateHistoryWhoForgotTwoDays() {
        Pair<LocalDateTime, LocalDateTime> dayBeforeYesterdayRange = getDayBeforeYesterdayRange();
        var today = LocalDate.now();
        LocalDate dayBeforeYesterday = today.minusDays(2);

        List<HistoryEntity> unfilledHistories = historyRepository.findAllByCreatedAtBetweenAndDoneIsNull(
                dayBeforeYesterdayRange.getFirst(),
                dayBeforeYesterdayRange.getSecond()
        );

        if (unfilledHistories.isEmpty()) {
            log.info("Нет красных отчетов за {}", dayBeforeYesterday);
            return;
        }
        Map<Long, List<HistoryEntity>> historiesByUser = unfilledHistories.stream()
                .collect(Collectors.groupingBy(h -> h.getGoal().getUserId()));

        historiesByUser.forEach((telegramId, histories) -> {
            UserEntity user = userService.getUser(telegramId);
            MarathonEntity marathon = user.getMarathon();

            LocalDate start = marathon.getDateStart().toLocalDate();
            LocalDate end = marathon.getDateEnd().toLocalDate();

            if (dayBeforeYesterday.isBefore(start) || dayBeforeYesterday.isAfter(end)) {
                return;
            }

            if (start.isAfter(dayBeforeYesterday)) {
                return;
            }

            histories.forEach(h -> h.setDone(false));
            historyRepository.saveAll(histories);

            histories.sort(Comparator.comparingInt(history -> history.getGoal().getPosition() == 0 ?
                    Integer.MAX_VALUE : history.getGoal().getPosition()));

            letterSender.sendRedReportWarning(user, histories);
        });
    }


    @Transactional
    public void createFinalDayReports(MarathonEntity marathon) {
        Pair<LocalDateTime, LocalDateTime> yesterdayRange = getYesterdayRange();
        LocalDateTime timeReport = yesterdayRange.getFirst().plusHours(12);
        List<HistoryEntity> historiesToSave = new ArrayList<>();

        List<UserEntity> users = userService.getUsersByMarathonId(marathon.getId());

        for (UserEntity user : users) {
            boolean reportExists = historyRepository.existsByTelegramIdAndCreatedAtBetween(
                    user.getTelegramId(),
                    yesterdayRange.getFirst(),
                    yesterdayRange.getSecond()
            );

            if (!reportExists) {
                List<GoalEntity> goals = goalService.getGoalByUser(user.getTelegramId());

                goals.forEach(goal -> {
                    var history = createHistoryGoal(goal, false, timeReport);
                    historiesToSave.add(history);
                });
                letterSender.sendBadReportFinal(user, goals);
            }
        }
        historyRepository.saveAll(historiesToSave);
    }


    /**
     * Подсчитывает количество не выполненных целей за марафон у пользователя
     */
    @Transactional
    public Pair<Long, Long> getCountFailByUserInMarathon(UserEntity user) {
        var history = historyRepository.findByTelegramIdAndCreatedAtBetween(
                user.getTelegramId(),
                user.getMarathon().getDateStart(),
                user.getMarathon().getDateEnd());

        var countFail = history.stream().filter(h -> h.getDone() != null && !h.getDone()).count();
        var countNull = history.stream().filter(h -> h.getDone() == null).count();
        return Pair.of(countFail, countFail + countNull);
    }

    /**
     * Получение штрафов за марафон
     */
    @Transactional
    public List<HistoryEntity> getFailByUserInMarathon(UserEntity user) {
        var history = historyRepository.findByTelegramIdAndCreatedAtBetween(
                user.getTelegramId(),
                user.getMarathon().getDateStart(),
                user.getMarathon().getDateEnd());

        return history.stream()
                .filter(his -> !his.getDone())
                .toList();
    }


    /**
     * Удаление отчета
     */
    @Transactional
    public void deleteReport(Long chatId) {

        Pair<LocalDateTime, LocalDateTime> todayRange = getTodayRange();

        var histories = historyRepository.findByTelegramIdAndCreatedAtBetween(chatId, todayRange.getFirst(),
                todayRange.getSecond());

        historyRepository.deleteAll(histories);
    }

    /**
     * Проверка выходного дня
     */
    public boolean isRestDay(MarathonEntity marathon, LocalDate localDate) {
        DayOfWeek day = localDate.getDayOfWeek();
        return (day == DayOfWeek.SATURDAY && Boolean.TRUE.equals(marathon.getRestOnSaturday()))
                || (day == DayOfWeek.SUNDAY && Boolean.TRUE.equals(marathon.getRestOnSunday()));
    }
}
