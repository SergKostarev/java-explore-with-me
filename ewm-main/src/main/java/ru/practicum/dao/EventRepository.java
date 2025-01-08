package ru.practicum.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.Event;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    @Query("select e from Event e JOIN FETCH e.category JOIN FETCH e.initiator " +
            "where e.initiator.id = ?1 order by e.id offset ?2 rows fetch next ?3 rows only")
    List<Event> findByInitiatorId(long initiatorId, int from, int size);

    List<Event> findByCategoryId(long categoryId);

}
