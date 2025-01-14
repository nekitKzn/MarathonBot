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

import static java.time.LocalDateTime.now;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final HistoryRepository historyRepository;
    private final GoalService goalService;
    private final UserService userService;
    private final LetterSender letterSender;

    @Transactional
    public List<Pair<String, Boolean>> createHistoryGoal(Long telegramId, List<Integer> optionIds) {

        var goals = goalService.getGoalByUser(telegramId);

        List<Pair<String, Boolean>> list = new ArrayList<>();

        goals.forEach(goal -> {
            if (goal.getPosition() == 5) {
                list.add(Pair.of(goal.getName(), true));
                var history = createHistoryGoal(goal, true, now());
                historyRepository.save(history);
            } else {
                var completed = optionIds.contains(goal.getPosition() - 1);
                list.add(Pair.of(goal.getName(), completed));
                var history = createHistoryGoal(goal, completed, now());
                historyRepository.save(history);
            }

        });
        return list;
    }

    @Transactional
    public List<Pair<String, Boolean>> updateHistory(Long telegramId, List<Integer> optionIds) {

        List<Pair<String, Boolean>> list = new ArrayList<>();

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay().minusDays(1);
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX).minusDays(1);
        var histories = historyRepository.findByTelegramIdAndCreatedAtBetween(telegramId, startOfDay, endOfDay);

        histories.forEach(history -> {
            var goal = history.getGoal();
            var completed = optionIds.contains(goal.getPosition() - 1);
            list.add(Pair.of(goal.getName(), completed));
            history.setDone(completed);
        });
        historyRepository.saveAll(histories);
        return list;
    }

    private HistoryEntity createHistoryGoal(GoalEntity goal, Boolean completed, LocalDateTime time) {
        return HistoryEntity.builder()
                .goal(goal)
                .done(completed)
                .createdAt(time)
                .build();
    }

    public boolean checkExistHistoryToday(Long telegramId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        return historyRepository.existsByTelegramIdAndCreatedAtBetween(telegramId, startOfDay, endOfDay);
    }

    public boolean checkExistNullHistoryYesterday(Long telegramId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay().minusDays(1);
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX).minusDays(1);
        return historyRepository.findByTelegramIdAndCreatedAtBetween(telegramId, startOfDay, endOfDay)
                .stream().anyMatch(history -> history.getDone() == null);
    }

    @Transactional(readOnly = true)
    public void sendWhoDidNotSetReport() {
        userService.getPlayingUsers()
                .forEach(user -> {
                    var needNotification = !checkExistHistoryToday(user.getTelegramId());
                    if (needNotification) {
                        letterSender.sendWhoDidNotSetReport(user);
                    }
                });
    }

    @Transactional
    public void createHistoryWhoFogot() {
        LocalDateTime endOfDay = now();
        LocalDateTime startOfDay = endOfDay.minusDays(1);
        userService.getPlayingUsers()
                .forEach(user -> {
                    var exitReport = historyRepository.existsByTelegramIdAndCreatedAtBetween(user.getTelegramId(),
                            startOfDay,
                            endOfDay);

                    if (!exitReport) {
                        var goals = goalService.getGoalByUser(user.getTelegramId());
                        goals.stream()
                                .filter(goal -> goal.getPosition() != 5)
                                .forEach(goal -> {
                                    var history = createHistoryGoal(goal, null, now().minusHours(12));
                                    historyRepository.save(history);
                                });
                        var goal5 = goals.stream()
                                .filter(goal -> goal.getPosition() == 5)
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("Goal 5 not found"));
                        var history = createHistoryGoal(goal5, false, now().minusHours(12));
                        historyRepository.save(history);
                        letterSender.sendBadReport(user, goals);
                    }
                });
    }

    public Pair<Long, Long> getCountFailByUserInMarathone(UserEntity user, MarathonEntity marathone) {
        var history = historyRepository.findByTelegramIdAndCreatedAtBetween(
                user.getTelegramId(),
                marathone.getDateStart(),
                marathone.getDateEnd());

        var countFail = history.stream().filter(h -> h.getDone() != null && !h.getDone()).count();
        var countNull = history.stream().filter(h -> h.getDone() == null).count();
        return Pair.of(countFail, countFail + countNull);
    }

    public void deleteReport(Long chatId) {

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        var histories = historyRepository.findByTelegramIdAndCreatedAtBetween(chatId, startOfDay, endOfDay);

        if (!histories.isEmpty()) {
            var user = userService.getUser(chatId);
            letterSender.publishInMarathonsByUserId(chatId, String.format("‼\uFE0F Отчет марафонца '%s' анулирован ‼\uFE0F",
                    user.getTelegramFirstName()));
        }
        historyRepository.deleteAll(histories);

    }
}
