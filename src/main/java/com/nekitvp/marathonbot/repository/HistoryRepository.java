package com.nekitvp.marathonbot.repository;

import com.nekitvp.marathonbot.model.HistoryEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoryRepository extends JpaRepository<HistoryEntity, Long> {

    @Query("SELECT COUNT(h) > 0 FROM HistoryEntity h WHERE h.goal.userId = :telegramId AND h.createdAt >= :startOfDay AND h.createdAt <= :endOfDay")
    boolean existsByTelegramIdAndCreatedAtBetween(@Param("telegramId") Long telegramId,
                                                  @Param("startOfDay") LocalDateTime startOfDay,
                                                  @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT h FROM HistoryEntity h WHERE h.goal.userId = :telegramId AND h.createdAt >= :startOfDay AND h.createdAt <= :endOfDay")
    List<HistoryEntity> findByTelegramIdAndCreatedAtBetween(Long telegramId, LocalDateTime startOfDay, LocalDateTime endOfDay);
}
