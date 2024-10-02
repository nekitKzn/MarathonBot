package com.nekitvp.marathonbot.repository;

import com.nekitvp.marathonbot.model.UserMarathonEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMarathonRepository extends JpaRepository<UserMarathonEntity, Long> {

    List<UserMarathonEntity> findByTelegramId(Long telegramId);

}
