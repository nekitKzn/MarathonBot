package com.nekitvp.marathonbot.repository;

import com.nekitvp.marathonbot.model.HistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoryRepository extends JpaRepository<HistoryEntity, Long> {

}
