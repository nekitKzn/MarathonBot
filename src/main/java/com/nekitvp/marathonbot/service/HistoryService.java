package com.nekitvp.marathonbot.service;


import com.nekitvp.marathonbot.model.GoalEntity;
import com.nekitvp.marathonbot.model.HistoryEntity;
import com.nekitvp.marathonbot.repository.HistoryRepository;
import java.time.LocalDateTime;
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
        var lastHistoryByTelegramId = historyRepository.findLastByTelegramId(telegramId);
        if (lastHistoryByTelegramId == null) {
            return false;
        }
        return lastHistoryByTelegramId.getCreatedAt().toLocalDate().equals(LocalDateTime.now().toLocalDate());
    }
}
