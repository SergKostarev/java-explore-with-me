package ru.practicum.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.Request;
import ru.practicum.model.RequestCountByEvent;
import ru.practicum.model.RequestStatus;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {

    Optional<Request> findByEventIdAndRequesterId(long eventId, long requesterId);

    List<Request> findByEventId(long eventId);

    List<Request> findByRequesterId(long requesterId);

    long countByEventIdAndStatus(long eventId, RequestStatus status);

    @Query("select new ru.practicum.model.RequestCountByEvent(r.event.id, count(r.id)) " +
            "from Request as r " +
            "where r.event.id in ?1 and r.status = ?2 " +
            "group by r.event.id")
    List<RequestCountByEvent> countByEventIdsAndStatus(List<Long> eventIds, RequestStatus status);
}
