package com.nekitvp.marathonbot.service;


import com.nekitvp.marathonbot.model.GoalEntity;
import com.nekitvp.marathonbot.model.HistoryEntity;
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
            var completed = optionIds.contains(goal.getPosition() - 1);
            list.add(Pair.of(goal.getName(), completed));
            var history = createHistoryGoal(goal, completed);
            historyRepository.save(history);
        });
        return list;
    }

    private HistoryEntity createHistoryGoal(GoalEntity goal, Boolean completed) {
        return HistoryEntity.builder()
                .goal(goal)
                .done(completed)
                .build();
    }

    public boolean checkExistHistoryToday(Long telegramId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        return historyRepository.existsByTelegramIdAndCreatedAtBetween(telegramId, startOfDay, endOfDay);
    }

    @Transactional(readOnly = true)
    public void sendWhoDidNotSetReport() {
        userService.getUsers().forEach(user -> {
            var needNotification = !checkExistHistoryToday(user.getTelegramId());
            if (needNotification) {
                letterSender.sendWhoDidNotSetReport(user);
            }
        });
    }

    @Transactional
    public void createHistoryWhoFogot() {
        LocalDateTime endOfDay = LocalDateTime.now();
        LocalDateTime startOfDay = endOfDay.minusDays(1);
        userService.getUsers().forEach(user -> {
            var exitReport = historyRepository.existsByTelegramIdAndCreatedAtBetween(user.getTelegramId(), startOfDay,
                    endOfDay);

            if (!exitReport) {
                var goals = goalService.getGoalByUser(user.getTelegramId());
                goals.stream()
                        .filter(goal -> goal.getPosition() != 5)
                        .forEach(goal -> {
                            var history = createHistoryGoal(goal, null);
                            historyRepository.save(history);
                        });
                var goal5 = goals.stream()
                        .filter(goal -> goal.getPosition() == 5)
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Goal 5 not found"));
                var history = createHistoryGoal(goal5, false);
                historyRepository.save(history);
                letterSender.sendBadReport(user, goals);
            }
        });
    }
}
