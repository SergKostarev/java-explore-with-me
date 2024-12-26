package ru.practicum;

import dto.EndpointHit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class EndpointHitTest {

    @Autowired
    private JacksonTester<EndpointHit> json;

    @Test
    public void testEndpointHit() throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime time = LocalDateTime.now();
        EndpointHit endpointHit = new EndpointHit(
                "192.163.0.1",
                "/events/1",
                time.format(formatter));
        JsonContent<EndpointHit> result = json.write(endpointHit);
        assertThat(result).extractingJsonPathStringValue("$.app").isEqualTo(endpointHit.getApp());
        assertThat(result).extractingJsonPathStringValue("$.uri").isEqualTo(endpointHit.getUri());
        assertThat(result).extractingJsonPathStringValue("$.ip").isEqualTo(endpointHit.getIp());
        assertThat(result).extractingJsonPathStringValue("$.timestamp").isEqualTo(endpointHit.getTimestamp().toString());
    }

}