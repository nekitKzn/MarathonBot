package com.nekitvp.marathonbot.repository;

import com.nekitvp.marathonbot.model.GoalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoalRepository extends JpaRepository<GoalEntity, Long> {

}
