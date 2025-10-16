package com.nekitvp.marathonbot.service;

import com.nekitvp.marathonbot.integration.yandex.service.YandexGptService;
import com.nekitvp.marathonbot.model.MarathonEntity;
import com.nekitvp.marathonbot.model.UserEntity;
import com.nekitvp.marathonbot.repository.MarathonRepository;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.nekitvp.marathonbot.util.DateTimeUtil.isRestDay;
import static com.nekitvp.marathonbot.util.MessageTemplate.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarathonService {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private final MarathonRepository marathonRepository;
    private final HistoryService historyService;
    private final LetterSender letterSender;
    private final UserService userService;
    private final YandexGptService yandexGptService;

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
    public List<MarathonEntity> getAllMarathonsForMotivate() {
        return getPlayingMarathone();
    }

    @Transactional(readOnly = true)
    public List<MarathonEntity> getAllMarathonsForEveningQuestion() {
        return getPlayingMarathone().stream()
                .filter(MarathonEntity::getEveningQuestionsEnabled)
                .toList();
    }

    private List<MarathonEntity> getPlayingMarathone() {
        return marathonRepository.findAll().stream()
                .filter(marathon -> marathon.getDateStart() != null)
                .filter(marathon -> marathon.getDateEnd() != null)
                .filter(marathon -> LocalDateTime.now().isBefore(marathon.getDateEnd()))
                .filter(marathon -> LocalDateTime.now().isAfter(marathon.getDateStart()))
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

    /**
     * Метод, для рассылки статистики активных марафонов
     */
    @Transactional
    public void sendStatistics() {
        getPlayingMarathone()
                .forEach(this::sendStatisticByMarathon);
    }

    private void sendStatisticByMarathon(MarathonEntity marathon) {
        Map<String, Pair<Long, Long>> mapUsers = getResultPersonMap(marathon);
        letterSender.sendStatistics(marathon, mapUsers);
    }

    private String sendFinalStatisticByMarathon(MarathonEntity marathon) {
        Map<String, Pair<Long, Long>> mapUsers = getResultPersonMap(marathon);
        return letterSender.sendFinalStatistics(marathon, mapUsers);
    }

    private Map<String, Pair<Long, Long>> getResultPersonMap(MarathonEntity marathon) {
        var users = userService.getUsersByMarathonId(marathon.getId());
        return users.stream()
                .collect(Collectors.toMap(
                        UserEntity::getTelegramFirstName,
                        historyService::getCountFailByUserInMarathon
                ));
    }

    /**
     * Метод, возвращающий булеан, а начался ли марафон
     */
    @Transactional
    public boolean marathonIsStarted(MarathonEntity marathon) {
        if (marathon.getDateStart() != null && marathon.getDateEnd() != null) {
            return LocalDateTime.now().isAfter(marathon.getDateStart()) &&
                    LocalDateTime.now().isBefore(marathon.getDateEnd());
        }
        return false;
    }

    @Transactional
    public void startMarathonsIfNeeded() {
        LocalDate today = LocalDate.now();
        var marathons = marathonRepository.findAll().stream()
                .filter(m -> m.getDateStart() != null)
                .filter(m -> m.getDateStart().toLocalDate().equals(today))
                .toList();

        if (marathons.isEmpty()) {
            log.info("No marathons starting today ({})", today);
            return;
        }

        marathons.forEach(m -> {
            log.info("Starting marathon: {}", m.getName());
            letterSender.publishEscape(m.getGroupId(), MARATHON_START,
                    m.getName(),
                    m.getDateStart().format(DATE_TIME_FORMATTER),
                    m.getDateEnd().format(DATE_TIME_FORMATTER)
            );
        });
    }

    @Transactional
    public void finishMarathonsIfNeeded() {
        LocalDate today = LocalDate.now();
        var marathons = marathonRepository.findAll().stream()
                .filter(m -> m.getDateEnd() != null)
                .filter(m -> m.getDateEnd().toLocalDate().equals(today.minusDays(1))) // закончились вчера
                .toList();

        if (marathons.isEmpty()) {
            log.info("No marathons finished yesterday ({})", today.minusDays(1));
            return;
        }

        marathons.forEach(m -> {
            log.info("Finishing marathon: {}", m.getName());

            if (!isRestDay(m, today.minusDays(1))) {
                historyService.createFinalDayReports(m);
            }

            letterSender.publishEscape(m.getGroupId(), MARATHON_FINISH, m.getName());

            sendFinalStatisticByMarathon(m);
            ;

            if (m.getManagers() != null) {
                m.getManagers().forEach(manager ->
                        letterSender.publishEscape(manager.getTelegramId(), MARATHON_FINISH_MANAGER, m.getName())
                );
            }
        });
    }

    @Transactional(readOnly = true)
    public void remindMarathonsStartingInHour() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        List<MarathonEntity> toRemind = marathonRepository.findAll().stream()
                .filter(m -> m.getDateStart() != null)
                .filter(m -> m.getGroupId() != null)
                .filter(m -> m.getDateStart().toLocalDate().equals(tomorrow))
                .toList();

        if (toRemind.isEmpty()) return;

        toRemind.forEach(m -> {
            String startStr = m.getDateStart().format(DATE_TIME_FORMATTER);

            letterSender.publishEscape(m.getGroupId(), MARATHON_START_REMINDER, m.getName(), startStr);

            if (m.getManagers() != null && !m.getManagers().isEmpty()) {
                m.getManagers().forEach(u ->
                        letterSender.publishEscape(u.getTelegramId(), MARATHON_START_REMINDER_MANAGER,
                                m.getName(), startStr));
            }
        });
    }

    @Transactional(readOnly = true)
    public void remindLastDay() {
        LocalDate today = LocalDate.now();

        List<MarathonEntity> lastDayMarathons = marathonRepository.findAll().stream()
                .filter(m -> m.getDateEnd() != null)
                .filter(m -> m.getDateEnd().toLocalDate().equals(today))
                .toList();

        if (lastDayMarathons.isEmpty()) {
            log.info("No marathons ending today ({})", today);
            return;
        }

        lastDayMarathons.forEach(m -> {
            log.info("Reminding last day for marathon: {}", m.getName());

            letterSender.publishEscape(m.getGroupId(), MARATHON_LAST_DAY, m.getName());

            if (m.getManagers() != null && !m.getManagers().isEmpty()) {
                m.getManagers().forEach(u ->
                        letterSender.publishEscape(u.getTelegramId(), MARATHON_LAST_DAY_MANAGER, m.getName())
                );
            }
        });
    }

    @Transactional(readOnly = true)
    public void remindRestDay() {
        LocalDate today = LocalDate.now();

        List<MarathonEntity> marathons = getPlayingMarathone().stream()
                .filter(m -> isRestDay(m, today))
                .toList();

        if (marathons.isEmpty()) return;

        marathons.forEach(m ->
                letterSender.publish(m.getGroupId(), MARATHON_REST_DAY)
        );
    }
}
