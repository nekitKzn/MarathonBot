package com.nekitvp.marathonbot.service;

import com.nekitvp.marathonbot.model.MarathonEntity;
import com.nekitvp.marathonbot.model.UserEntity;
import com.nekitvp.marathonbot.repository.MarathonRepository;
import jakarta.ws.rs.NotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MarathonService {

    private final MarathonRepository marathonRepository;
    private final HistoryService historyService;
    private final LetterSender letterSender;
    private final UserService userService;

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

    @Transactional(readOnly = true)
    public List<MarathonEntity> getAllMarathonesForMotivate() {
        return getPlayingMarathone();
    }

    private List<MarathonEntity> getPlayingMarathone() {
        return marathonRepository.findAll().stream()
                .filter(MarathonEntity::getIsWork)
                .toList();
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

    @Transactional
    public void sendStatistics() {
        var listMarathons = getPlayingMarathone();
        listMarathons.stream()
                .filter(marathon -> marathon.getDateStart() != null)
                .filter(marathon -> marathon.getDateEnd() != null)
                .filter(marathon -> LocalDateTime.now().isBefore(marathon.getDateEnd()))
                .filter(marathon -> LocalDateTime.now().isAfter(marathon.getDateStart()))
                .forEach(marathon -> {
                    var users = userService.getUsersByMarathonId(marathon.getId());
                    Map<String, Pair<Long, Long>> mapUsers = users.stream()
                            .collect(Collectors.toMap(
                                    UserEntity::getTelegramFirstName,
                                    historyService::getCountFailByUserInMarathone
                            ));
                    letterSender.sendStatistics(marathon, mapUsers);
                });
    }

    @Transactional
    public boolean marathonIsStarted(MarathonEntity marathon) {
        if (marathon.getDateStart() != null && marathon.getDateEnd() != null) {
            return LocalDateTime.now().isAfter(marathon.getDateStart()) &&
                    LocalDateTime.now().isBefore(marathon.getDateEnd());
        }
        return false;
    }
}
