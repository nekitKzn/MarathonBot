package com.nekitvp.marathonbot.service;

import com.nekitvp.marathonbot.model.GoalEntity;
import com.nekitvp.marathonbot.repository.GoalRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;

    @Transactional(readOnly = true)
    public List<GoalEntity> getGoalByUser(Long chatId) {
        return goalRepository.findAllByUserIdOrderByPosition(chatId);
    }
}
