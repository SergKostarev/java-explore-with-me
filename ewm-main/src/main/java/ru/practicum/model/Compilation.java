package ru.practicum.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "compilations")
public class Compilation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Boolean pinned;

    private String title;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "compilation_event",
            joinColumns = { @JoinColumn(name = "compilation_id") },
            inverseJoinColumns = { @JoinColumn(name = "event_id")})
    private Set<Event> events = new HashSet<>();

}
