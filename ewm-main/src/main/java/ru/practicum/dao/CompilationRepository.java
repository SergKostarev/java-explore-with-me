package ru.practicum.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.Compilation;

import java.util.List;
import java.util.Optional;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    Optional<Compilation> findByTitleIgnoreCase(String title);

    @Query("select c from Compilation c where (?1 is null or c.pinned = ?1) order by c.id offset ?2 rows fetch next ?3 rows only")
    List<Compilation> getCompilations(Boolean pinned, int from, int size);
}