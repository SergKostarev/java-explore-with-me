package ru.practicum.service;

import dto.EndpointHit;
import dto.ViewStats;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dao.StatsRepository;
import ru.practicum.handler.IncorrectDataException;
import ru.practicum.mapper.StatsMapper;
import ru.practicum.model.EndpointHitEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsService {

    private final StatsRepository statsRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Transactional
    public void create(EndpointHit hit) {
        statsRepository.save(StatsMapper.toEndpointHitEntity(hit));
    }

    public List<ViewStats> get(String start, String end, String[] uris, boolean unique) {
        LocalDateTime startDate;
        try {
            startDate = convertDate(start);
        } catch (DateTimeParseException e) {
            throw new IncorrectDataException("Неверный формат даты", start);
        }
        LocalDateTime endDate;
        try {
            endDate = convertDate(end);
        } catch (DateTimeParseException e) {
            throw new IncorrectDataException("Неверный формат даты", end);
        }
        if (endDate.isBefore(startDate)) {
            throw new IncorrectDataException("Дата окончания не должна быть раньше начала периода", end);
        }
        List<ViewStats> vsList = new ArrayList<>();
        List<EndpointHitEntity> hitsList;
        if (uris == null || uris.length == 0) {
            hitsList = statsRepository
                    .findByTimestampIsAfterAndTimestampIsBefore(startDate, endDate);
            Map<String, List<EndpointHitEntity>> hits = hitsList
                    .stream()
                    .collect(Collectors.groupingBy(EndpointHitEntity::getUri));
            for (Map.Entry<String, List<EndpointHitEntity>> e: hits.entrySet()) {
                vsList.add(new ViewStats(e.getKey(),
                        unique ? getDistinctIpCount(e.getValue()) : e.getValue().size()));
            }
        } else {
            hitsList = statsRepository
                    .findByTimestampIsAfterAndTimestampIsBeforeAndUriIn(startDate, endDate, uris);
            Map<String, List<EndpointHitEntity>> hits = hitsList
                    .stream()
                    .collect(Collectors.groupingBy(EndpointHitEntity::getUri));
            for (String uri : uris) {
                if (hits.containsKey(uri)) {
                    List<EndpointHitEntity> e = hits.get(uri);
                    vsList.add(new ViewStats(uri,
                            unique ? getDistinctIpCount(e) : e.size()));
                } else {
                    vsList.add(new ViewStats(uri, 0L));
                }
            }
        }
        Collections.sort(vsList, Comparator.comparingLong(ViewStats::getHits).reversed());
        return vsList;
    }

    private LocalDateTime convertDate(String stringDate) throws DateTimeParseException {
        return LocalDateTime.parse(stringDate, formatter);
    }

    private long getDistinctIpCount(List<EndpointHitEntity> hits) {
        return hits
                .stream()
                .map(EndpointHitEntity::getIp)
                .distinct().count();
    }
}