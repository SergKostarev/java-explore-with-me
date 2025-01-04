package ru.practicum.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.StatsClient;

@Configuration
public class AppConfig {

    @Value("${stats-ewm-serv.url}")
    private String statsServicePath;

    @Bean
    public StatsClient getStatsClient() {
        return new StatsClient(statsServicePath);
    }
}