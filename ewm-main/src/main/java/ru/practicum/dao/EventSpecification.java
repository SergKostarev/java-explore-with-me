package ru.practicum.dao;

import org.springframework.data.jpa.domain.Specification;
import ru.practicum.model.Event;
import ru.practicum.model.EventState;

import java.time.LocalDateTime;
import java.util.List;

public class EventSpecification {

    public static Specification<Event> initiatorIdIn(List<Long> users) {
        return (root, query, cb) -> root.join("initiator").get("id").in(users);
    }

    public static Specification<Event> stateIn(List<EventState> states) {
        return (root, query, cb) -> root.get("state").in(states);
    }

    public static Specification<Event> annotationOrDescriptionContainsTextIgnoreCase(String text) {
        return (root, query, cb) ->
                cb.or(cb.like(cb.lower(root.get("annotation")), text.toLowerCase()),
                        cb.like(cb.lower(root.get("description")), text.toLowerCase()));
    }

    public static Specification<Event> categoryIdIn(List<Long> categoriesIds) {
        return (root, query, cb) -> root.join("category").get("id").in(categoriesIds);
    }

    public static Specification<Event> isPaid(boolean paid) {
        return (root, query, cb) -> cb.equal(root.get("paid"), paid);
    }

    public static Specification<Event> startIsGreaterThanOrEqualTo(LocalDateTime date) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("eventDate"), date);
    }

    public static Specification<Event> endIsLessThanOrEqualTo(LocalDateTime date) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("eventDate"), date);
    }
}
