package ru.practicum.service;

import dto.EndpointHit;
import dto.ViewStats;
import exception.IncorrectDateException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dao.StatsRepository;
import ru.practicum.mapper.StatsMapper;
import ru.practicum.model.EndpointHitEntity;
import utils.DateUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsService {

    private final StatsRepository statsRepository;

    @Transactional
    public void create(EndpointHit hit) {
        statsRepository.save(StatsMapper.toEndpointHitEntity(hit));
    }

    public List<ViewStats> get(String start, String end, String[] uris, boolean unique) {
        LocalDateTime startDate = DateUtils.convertToDate(start);
        LocalDateTime endDate = DateUtils.convertToDate(end);
        if (endDate.isBefore(startDate)) {
            throw new IncorrectDateException("Дата окончания не должна быть раньше начала периода", end);
        }
        List<ViewStats> vsList = new ArrayList<>();
        List<EndpointHitEntity> hitsList;
        if (uris == null || uris.length == 0) {
            hitsList = statsRepository
                    .findByTimestampIsGreaterThanEqualAndTimestampIsLessThanEqual(startDate, endDate);
            Map<String, List<EndpointHitEntity>> hits = hitsList
                    .stream()
                    .collect(Collectors.groupingBy(EndpointHitEntity::getUri));
            for (Map.Entry<String, List<EndpointHitEntity>> e: hits.entrySet()) {
                vsList.add(new ViewStats(e.getKey(),
                        unique ? getDistinctIpCount(e.getValue()) : e.getValue().size()));
            }
        } else {
            hitsList = statsRepository
                    .findByTimestampIsGreaterThanEqualAndTimestampIsLessThanEqualAndUriIn(startDate, endDate, uris);
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

    private long getDistinctIpCount(List<EndpointHitEntity> hits) {
        return hits
                .stream()
                .map(EndpointHitEntity::getIp)
                .distinct().count();
    }
}