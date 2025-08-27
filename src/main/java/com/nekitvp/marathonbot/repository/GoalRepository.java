package com.nekitvp.marathonbot.repository;

import com.nekitvp.marathonbot.model.GoalEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoalRepository extends JpaRepository<GoalEntity, Long> {

    List<GoalEntity> findAllByUserIdAndMarathonIdOrderByPosition(Long userId, Long marathonId);
}
