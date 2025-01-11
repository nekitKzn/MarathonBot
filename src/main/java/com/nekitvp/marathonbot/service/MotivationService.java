package com.nekitvp.marathonbot.service;

import com.nekitvp.marathonbot.model.MarathonEntity;
import com.nekitvp.marathonbot.model.MotivationEntity;
import com.nekitvp.marathonbot.repository.MotivationRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MotivationService {

    private final MotivationRepository motivationRepository;
    private final MarathonService marathonService;
    private final LetterSender letterSender;

    @Transactional
    public void sendRandomMotivation() {
        List<MarathonEntity> listMarathonForMotivation = marathonService.getAllMarathonesForMotivate();
        listMarathonForMotivation.forEach(marathon -> {

            Optional<MotivationEntity> randomMotivation = motivationRepository.findRandomByMarathonIdAndIsSendFalse(marathon.getId());
            if (randomMotivation.isPresent()) {
                var motivation = randomMotivation.get();
                letterSender.sendMotivation(motivation);
                motivation.setIsSend(true);
                motivationRepository.save(motivation);
            }
        });
    }
}
