package ru.practicum;

import dto.EndpointHit;
import dto.ViewStats;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.List;
import java.util.Map;

@Component
@EnableConfigurationProperties(StatsServiceProperties.class)
public class StatsClient {

    private final RestTemplate rest;

    public StatsClient(RestTemplateBuilder builder, StatsServiceProperties props) {
        rest = builder.uriTemplateHandler(new DefaultUriBuilderFactory(props.url()))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build();
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    public void postHit(EndpointHit body) throws RestClientResponseException {
        HttpEntity<EndpointHit> requestEntity = new HttpEntity<>(body, defaultHeaders());
        rest.exchange("/hit", HttpMethod.POST, requestEntity, Object.class);
    }

    public List<ViewStats> getStats(String start, String end, String[] uris, boolean unique) throws RestClientResponseException {
        Map<String, Object> parameters = Map.of(
                "start", start,
                "end", end,
                "uris", uris,
                "unique", unique
        );
        HttpEntity requestEntity = new HttpEntity<>(null, defaultHeaders());
        return rest
                .exchange("/stats?start={start}&end={end}&uris={uris}&unique={unique}",
                        HttpMethod.GET, requestEntity, new ParameterizedTypeReference<List<ViewStats>>(){}, parameters)
                .getBody();
    }
}