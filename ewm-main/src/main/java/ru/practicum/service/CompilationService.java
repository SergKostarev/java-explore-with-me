package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dao.CompilationRepository;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.NewCompilationDto;
import ru.practicum.dto.UpdateCompilationRequest;
import ru.practicum.exception.DataIntegrityException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationService {

    private final CompilationRepository compilationRepository;

    private final EventService eventService;

    @Transactional()
    public CompilationDto post(NewCompilationDto compilationDto) {
        String title = compilationDto.getTitle();
        boolean compilationExists = compilationRepository.findByTitleIgnoreCase(title).isPresent();
        if (compilationExists) {
            throw new DataIntegrityException(String.format("Compilation %s already exists.", title), title);
        }
        List<Long> eventsIds = new ArrayList<>();
        if (compilationDto.getEvents() != null) {
            compilationDto
                    .getEvents()
                    .stream()
                    .distinct()
                    .forEach(eventsIds::add);
        }
        List<Event> events = eventService.getByIds(eventsIds);
        Compilation savedCompilation = compilationRepository.save(
                CompilationMapper.toCompilation(compilationDto, events));
        return CompilationMapper.toCompilationDto(savedCompilation,
                eventService.getEventShortDto(savedCompilation.getEvents()));
    }


    @Transactional()
    public void delete(long compId) {
        Compilation compilation = getById(compId);
        compilationRepository.delete(compilation);
    }

    public Compilation getById(long id) {
        Optional<Compilation> compilation = compilationRepository.findById(id);
        if (compilation.isEmpty()) {
            throw new NotFoundException(String.format("Compilation with id=%d was not found.", id), id);
        }
        return compilation.get();
    }

    @Transactional()
    public CompilationDto update(long compId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = getById(compId);
        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }
        if (updateCompilationRequest.getTitle() != null) {
            compilation.setTitle(updateCompilationRequest.getTitle());
        }
        if (updateCompilationRequest.getEvents() != null) {
            List<Long> newEventIds = updateCompilationRequest
                    .getEvents()
                    .stream()
                    .distinct()
                    .toList();
            List<Event> events = eventService.getByIds(newEventIds);
            compilation.getEvents().clear();
            compilation.getEvents().addAll(events);
        }
        Compilation updatedCompilation = compilationRepository.save(compilation);
        return CompilationMapper.toCompilationDto(updatedCompilation,
                eventService.getEventShortDto(updatedCompilation.getEvents()));
    }

    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        List<Compilation> compilations = compilationRepository.getCompilations(pinned, from, size);
        Set<Event> compilationEvents = compilations
                .stream()
                .map(Compilation::getEvents)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
        Map<Long, EventShortDto> eventShortDtoMap = eventService
                .getEventShortDto(compilationEvents)
                .stream()
                .collect(Collectors.toMap(EventShortDto::getId, Function.identity()));
        List<CompilationDto> compilationDtoList = new ArrayList<>();
        for (Compilation c : compilations) {
            List<EventShortDto> eventShortDto = c.getEvents()
                    .stream()
                    .map(e -> eventShortDtoMap.get(e.getId()))
                    .toList();
            compilationDtoList.add(CompilationMapper.toCompilationDto(c, eventShortDto));
        }
        return compilationDtoList;
    }

    public CompilationDto getCompilationsById(Long compId) {
        Compilation compilation = getById(compId);
        return CompilationMapper.toCompilationDto(compilation,
                eventService.getEventShortDto(compilation.getEvents()));
    }
}
