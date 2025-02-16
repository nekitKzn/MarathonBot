package com.nekitvp.marathonbot.service;


import com.nekitvp.marathonbot.model.GoalEntity;
import com.nekitvp.marathonbot.model.HistoryEntity;
import com.nekitvp.marathonbot.model.MarathonEntity;
import com.nekitvp.marathonbot.model.UserEntity;
import com.nekitvp.marathonbot.repository.HistoryRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.nekitvp.marathonbot.util.DateTimeUtil.getTodayRange;
import static com.nekitvp.marathonbot.util.DateTimeUtil.getYesterdayRange;
import static java.time.LocalDateTime.now;

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
     * Отправляем напоминание тем, кто не отправил отчет
     */
    @Transactional(readOnly = true)
    public void sendWhoDidNotSetReport() {
        userService.getUsersWhoHasActiveMarathone().forEach(user -> {
            if (!checkExistHistoryToday(user.getTelegramId())) {
                letterSender.sendWhoDidNotSetReport(user);
            }
        });
    }

    /**
     * Создаем отчеты за тех, кто не отправил за день
     */
    @Transactional
    public void createHistoryWhoForgot() {
        Pair<LocalDateTime, LocalDateTime> yesterdayRange = getYesterdayRange();
        userService.getUsersWhoHasActiveMarathone()
                .forEach(user -> {
                    var reportExists = historyRepository.existsByTelegramIdAndCreatedAtBetween(user.getTelegramId(),
                            yesterdayRange.getFirst(),
                            yesterdayRange.getSecond());

                    if (!reportExists) {
                        var goals = goalService.getGoalByUser(user.getTelegramId());
                        goals.stream()
                                .filter(goal -> goal.getPosition() != 0)
                                .forEach(goal -> {
                                    var history = createHistoryGoal(goal, null, now().minusHours(12));
                                    historyRepository.save(history);
                                });
                        var goalReport = goals.stream()
                                .filter(goal -> goal.getPosition() == 0)
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("Goal Report not found"));
                        var history = createHistoryGoal(goalReport, false, now().minusHours(12));
                        historyRepository.save(history);
                        letterSender.sendBadReport(user, goals);
                    }
                });
    }

    /**
     * Подсчитывает количество не выполненных целей за марафон у пользователя
     */
    @Transactional
    public Pair<Long, Long> getCountFailByUserInMarathone(UserEntity user) {
        var history = historyRepository.findByTelegramIdAndCreatedAtBetween(
                user.getTelegramId(),
                user.getMarathon().getDateStart(),
                user.getMarathon().getDateEnd());

        var countFail = history.stream().filter(h -> h.getDone() != null && !h.getDone()).count();
        var countNull = history.stream().filter(h -> h.getDone() == null).count();
        return Pair.of(countFail, countFail + countNull);
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
}
