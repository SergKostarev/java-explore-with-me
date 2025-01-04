package ru.practicum.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select u from User u order by u.id offset ?1 rows fetch next ?2 rows only")
    List<User> get(int from, int size);

    @Query("select u from User u where u.id in ?1 order by u.id")
    List<User> get(List<Long> ids);

    Optional<User> findByEmailIgnoreCase(String name);

}
