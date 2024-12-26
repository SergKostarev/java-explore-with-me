package ru.practicum.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.EndpointHitEntity;

import java.time.LocalDateTime;
import java.util.List;


public interface StatsRepository extends JpaRepository<EndpointHitEntity, Long> {
    List<EndpointHitEntity> findByTimestampIsAfterAndTimestampIsBeforeAndUriIn(
            LocalDateTime start,
            LocalDateTime end,
            String[] uris);

    List<EndpointHitEntity> findByTimestampIsAfterAndTimestampIsBefore(
            LocalDateTime start,
            LocalDateTime end);
}