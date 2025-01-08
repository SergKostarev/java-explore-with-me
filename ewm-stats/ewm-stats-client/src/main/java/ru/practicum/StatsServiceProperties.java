package ru.practicum;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "stats-ewm-serv")
public record StatsServiceProperties(String url) {}
