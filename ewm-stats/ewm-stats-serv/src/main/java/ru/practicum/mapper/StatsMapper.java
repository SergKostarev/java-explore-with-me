package ru.practicum.mapper;

import dto.EndpointHit;
import ru.practicum.model.EndpointHitEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StatsMapper {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static EndpointHitEntity toEndpointHitEntity(EndpointHit endpointHit) {
        EndpointHitEntity endpointHitEntity = new EndpointHitEntity();
        endpointHitEntity.setIp(endpointHit.getIp());
        endpointHitEntity.setUri(endpointHit.getUri());
        endpointHitEntity.setApp(endpointHit.getApp());
        endpointHitEntity.setTimestamp(LocalDateTime.parse(endpointHit.getTimestamp(), formatter));
        return endpointHitEntity;
    }
}