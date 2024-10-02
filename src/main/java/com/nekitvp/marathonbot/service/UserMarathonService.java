package com.nekitvp.marathonbot.service;

import com.nekitvp.marathonbot.model.MarathonEntity;
import com.nekitvp.marathonbot.model.UserMarathonEntity;
import com.nekitvp.marathonbot.repository.UserMarathonRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserMarathonService {

    private final UserMarathonRepository userMarathonRepository;

    @Transactional
    public List<Long> getGroupIdsByTelegramId(Long telegramId) {
        var list = userMarathonRepository.findByTelegramId(telegramId);
        return list.stream()
                .map(UserMarathonEntity::getMarathon)
                .map(MarathonEntity::getGroupId).toList();
    }
}
