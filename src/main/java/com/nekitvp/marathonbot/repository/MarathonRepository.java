package com.nekitvp.marathonbot.repository;

import com.nekitvp.marathonbot.model.MarathonEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarathonRepository extends JpaRepository<MarathonEntity, Long> {

    Optional<MarathonEntity> findByGroupId(Long groupId);
}
