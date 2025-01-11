package com.nekitvp.marathonbot.repository;

import com.nekitvp.marathonbot.model.MotivationEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MotivationRepository extends JpaRepository<MotivationEntity, Long> {

    @Query(value = "SELECT * FROM motivation WHERE marathon_id = :marathonId AND is_send = false ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<MotivationEntity> findRandomByMarathonIdAndIsSendFalse(@Param("marathonId") Long marathonId);

}
