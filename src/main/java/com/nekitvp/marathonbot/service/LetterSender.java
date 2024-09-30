package com.nekitvp.marathonbot.service;

import com.nekitvp.marathonbot.event.SendTelegramMessageEvent;
import com.nekitvp.marathonbot.model.GoalEntity;
import com.nekitvp.marathonbot.model.UserEntity;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Service
@Slf4j
@RequiredArgsConstructor
public class LetterSender {

    private final ApplicationEventPublisher publisher;

    private static final Random random = new Random();

    @Value("${bot.groupChatId}")
    private Long groupChatId;

    public void sendReport(String name, List<Pair<String, Boolean>> report) {

        StringBuilder reportBuilder = getShapka(name);

        for (Pair<String, Boolean> entry : report) {
            String result = Boolean.TRUE.equals(entry.getSecond()) ? "✅" : "❌";
            reportBuilder.append(result).append(" - ").append(entry.getFirst()).append("\n");
        }

        String text = reportBuilder.toString();
        publisher.publishEvent(new SendTelegramMessageEvent(this, text, groupChatId));
    }

    private static StringBuilder getShapka(String name) {
        StringBuilder reportBuilder = new StringBuilder();
        var date = LocalDateTime.now();
        reportBuilder.append("Отчет: ").append(name).append("\n");
        reportBuilder.append("Дата: ").append(date.toLocalDate());
        reportBuilder.append(" ").append(date.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))).append("\n\n");
        return reportBuilder;
    }

    private void publish(Long to, String template, Object... args) {
        String text = isEmpty(args) ? template : String.format(template, args);
        publisher.publishEvent(new SendTelegramMessageEvent(this, text, to));
    }

    public void sendWhoDidNotSetReport(UserEntity user) {
        // Список возможных сообщений
        List<String> phrases = List.of(
                "Брат, %s! 💪 Не вижу еще твоего отчета! Жду с нетерпением! \n\nБеспокоюсь о твоем финансовом положении 💵",
                "Эй, %s! 💼 Ты забыл про отчет! Напиши мне его как можно скорее!\n\nТвои деньги уже волнуются 💰",
                "Привет, %s! ⏳ Напоминаю, что я еще жду твой отчет! Не заставляй меня переживать за твои финансы 📉",
                "%s, давай не откладывай! 🤝 Я жду твой отчет. Не давай своим деньгам скучать! 💸",
                "%s, твой отчет ждет! 📝 Не заставляй его грустить в одиночестве. Мечтаю увидеть его как можно скорее!",
                "Йо, %s! 📊 Ты как там с отчетом? Жду, не дождусь! Пусть твои цифры танцуют от радости! 💃🕺",
                "Ну что, %s? 👀 Я жду твой отчет! Сделай так, чтобы он прибежал ко мне первым делом! 🚀",
                "Привет, %s! 💬 Не забудь про отчет, а то он потеряется без твоего внимания! Мы все переживаем! 🙈",
                "%s, у тебя там все ок? 😎 Отчета все еще не вижу! Давай, не подводи команду! 🚀",
                "Хэй, %s! 🏆 Побеждай с отчетом! Вся команда ждет, когда ты сделаешь следующий шаг. 🎯",
                "Здорово, %s! 📈 Твой отчет – это шаг к успеху. Мы все держим за тебя кулаки! 🤞",
                "%s, ты знаешь, что твой отчет — это ключ к богатству? 💰 Не теряй шанс, отправляй скорее!",
                "Алоха, %s! 🌺 Твой отчет вызывает волнения на финансовом рынке. Поделись им со мной скорее! 🌊",
                "Салют, %s! 🎉 Я все еще жду твой отчет. Сделай этот день лучше, отправь его прямо сейчас!",
                "%s, я тут подумал... а где твой отчет? 🧐 Не томи, я очень жду его появления!",
                "Эй, %s! 🚨 Срочная новость: без твоего отчета мир финансов трещит по швам! Жду его с нетерпением!",
                "Привет, %s! 🔥 Твой отчет – это огонь! Так что не затягивай, закинь его мне как можно скорее! 🚀",
                "%s, ну что там с отчетом? 😇 Мы все в ожидании твоего финального штриха! 🎨",
                "%s, если ты отправишь отчет, то сегодня произойдет что-то волшебное! 🪄 Давай попробуем?"
        );

        String randomPhrase = phrases.get(random.nextInt(phrases.size()));

        log.info("Напоминание отправлено пользователю {}: {}", user.getTelegramFirstName(), randomPhrase);
        publish(user.getTelegramId(), randomPhrase, user.getTelegramFirstName());
    }

    public void sendBadReport(UserEntity user, List<GoalEntity> goals) {
        StringBuilder reportBuilder = getShapka(user.getTelegramFirstName());

        reportBuilder.append("Увы, братишка не справился! \uD83E\uDD72").append("\n\n");

        var list = goals.stream()
                .filter(goal -> goal.getPosition() != 5)
                .toList();

        for (GoalEntity goal : list) {
            reportBuilder.append("❓").append(" - ").append(goal.getName()).append("\n");
        }

        reportBuilder.append("❌").append(" - ").append("Отчет");

        String text = reportBuilder.toString();
        publisher.publishEvent(new SendTelegramMessageEvent(this, text, groupChatId));


    }
}
