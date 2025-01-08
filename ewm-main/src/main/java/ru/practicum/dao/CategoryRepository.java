package ru.practicum.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("select c from Category c order by c.id offset ?1 rows fetch next ?2 rows only")
    List<Category> get(int from, int size);

    Optional<Category> findByNameIgnoreCase(String name);

}