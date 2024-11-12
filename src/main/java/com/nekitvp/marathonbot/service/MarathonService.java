package com.nekitvp.marathonbot.service;

import com.nekitvp.marathonbot.model.MarathonEntity;
import com.nekitvp.marathonbot.repository.MarathonRepository;
import jakarta.ws.rs.NotFoundException;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MarathonService {

    private final MarathonRepository marathonRepository;

    @Transactional
    public void addMarathon(String name, Long groupId) {
        var entity = marathonRepository.findByGroupId(groupId);
        MarathonEntity marathon;
        if (entity.isPresent()) {
            marathon = entity.get();
            marathon.setName(name);
            marathon.setIsMember(true);
        } else {
            marathon = MarathonEntity.builder()
                    .name(name)
                    .groupId(groupId)
                    .isMember(true)
                    .build();
        }
        marathonRepository.save(marathon);
    }

    @Transactional
    public void leftMarathon(Long groupId) {
        var entity = marathonRepository.findByGroupId(groupId);
        if (entity.isPresent()) {
            var marathon = entity.get();
            marathon.setIsMember(false);
            marathonRepository.save(marathon);
        }
    }

    public Map<Long, String> getAllActiveMarathones() {
        return marathonRepository.findAll()
                .stream().filter(MarathonEntity::getIsMember)
                .collect(Collectors.toMap(MarathonEntity::getId, MarathonEntity::getName));
    }

    public void selectMarathon(Long id) {
        var entity = marathonRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Марафон не найден"));
        entity.setSelect(true);
        marathonRepository.save(entity);
    }

    public Long getIdSelectMarathon() {
        var entity = marathonRepository.findBySelectTrue()
                .orElseThrow(() -> new NotFoundException("Марафон не выбран"));
        entity.setSelect(false);
        marathonRepository.save(entity);
        return entity.getGroupId();
    }
}
