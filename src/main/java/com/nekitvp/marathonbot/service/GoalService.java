package com.nekitvp.marathonbot.service;

import com.nekitvp.marathonbot.model.GoalEntity;
import com.nekitvp.marathonbot.repository.GoalRepository;
import com.nekitvp.marathonbot.util.MarkdownUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.nekitvp.marathonbot.util.DateTimeUtil.isRestDay;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;

    private final UserService userService;
    private final TemplatePhraseService templatePhraseService;
    private final LetterSender letterSender;

    @Lazy
    @Autowired
    private MarathonService marathonService;

    /**
     * Достаем актуальные цели у пользователя
     */
    @Transactional(readOnly = true)
    public List<GoalEntity> getGoalByUser(Long chatId) {
        var user = userService.getUser(chatId);
        var marathonId = user.getMarathonId();
        return goalRepository.findAllByUserIdAndMarathonIdOrderByPosition(chatId, marathonId);
    }

    @Transactional
    public void sendEveningQuestion() {
        LocalDate today = LocalDate.now();
        List<GoalEntity> listForSave = new ArrayList<>();
        marathonService.getAllMarathonsForEveningQuestion()
                .forEach(marathon -> {

                    if (isRestDay(marathon, today)) {
                        log.info("Skip Question, rest day in marathon {}", marathon.getName());
                        return;
                    }

                    GoalEntity goal = goalRepository.findGoalForEveningQuestion(marathon.getId());
                    if (goal == null) return;

                    var user = userService.getUser(goal.getUserId());

                    var template = templatePhraseService.getRandomEveningQuestion();
                    var goalName = MarkdownUtil.escape(goal.getName());
                    var userName = MarkdownUtil.escape(user.getTelegramFirstName());

                    letterSender.publish(marathon.getGroupId(), String.format(template, userName, goalName));

                    goal.setAskedCount(goal.getAskedCount() + 1);
                    listForSave.add(goal);
                });
        goalRepository.saveAll(listForSave);
    }
}
