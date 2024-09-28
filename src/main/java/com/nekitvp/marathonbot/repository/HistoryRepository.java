package com.nekitvp.marathonbot.repository;

import com.nekitvp.marathonbot.model.HistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoryRepository extends JpaRepository<HistoryEntity, Long> {

    @Query("SELECT h FROM HistoryEntity h WHERE h.goal.userId = ?1 ORDER BY h.id DESC LIMIT 1")
    HistoryEntity findLastByTelegramId(Long telegramId);

}
