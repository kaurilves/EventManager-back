package org.hometask.repositories;

import org.hometask.entities.EventEntity;
import org.hometask.entities.ParticipantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@Resource
public interface ParticipantRepository extends JpaRepository<ParticipantEntity, UUID> {

    @Query("select p from ParticipantEntity p where p.eventId = ?1")
    List<ParticipantEntity> findParticipantsBy(UUID eventId);

    void deleteAllByEventId(UUID eventId);
}
