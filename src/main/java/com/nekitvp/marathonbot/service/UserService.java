package com.nekitvp.marathonbot.service;

import com.nekitvp.marathonbot.enumBot.StateBot;
import com.nekitvp.marathonbot.model.UserEntity;
import com.nekitvp.marathonbot.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;


    @Transactional
    public StateBot getUserState(Long telegramUserId, Message message) {
        UserEntity userEntity = userRepository.findById(telegramUserId)
                .orElseGet(() -> userRepository.save(createUserEntity(message)));
        return userEntity.getState();
    }

    @Transactional
    public StateBot updateUserState(Long telegramUserId, StateBot state) {
        UserEntity userEntity = userRepository.findById(telegramUserId)
                .orElseThrow(() -> new RuntimeException("Акканут не найден: " + telegramUserId));
        userEntity.setState(state);
        userEntity.setCountChangeState(userEntity.getCountChangeState() + 1);
        userEntity.setCountChangeStateAll(userEntity.getCountChangeStateAll() + 1);
        userEntity.setUpdatedAt(LocalDateTime.now());
        userRepository.save(userEntity);
        return userEntity.getState();
    }

    @Transactional
    public boolean checkIsAdmin(Long id) {
        return userRepository.existsByTelegramIdAndAdminIsTrue(id);
    }

    @Transactional
    public UserEntity findByUserName(String userName) {
        return userRepository.findByTelegramUserName(userName)
                .orElse(null);
    }

    @Transactional
    public UserEntity findByTelegramId(Long telegramId) {
        return userRepository.findByTelegramId(telegramId).orElse(null);
    }

    private UserEntity createUserEntity(Message message) {
        return UserEntity.builder()
                .telegramId(message.getFrom().getId())
                .telegramUserName(message.getFrom().getUserName())
                .telegramFirstName(message.getFrom().getFirstName())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .state(StateBot.START)
                .build();
    }

    @Transactional
    public void saveAdmin(UserEntity user) {
        user.setAdmin(true);
        userRepository.save(user);
    }

    @Transactional
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public void resetCount() {
        List<UserEntity> users = userRepository.findAll().stream().peek(user -> user.setCountChangeState(0L)).toList();
        userRepository.saveAll(users);
    }

    @Transactional(readOnly = true)
    public List<UserEntity> getUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public UserEntity getUser(Long telegramId) {
        return userRepository.findByTelegramId(telegramId).orElse(null);
    }
}