package ru.practicum;

import dto.ViewStats;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ViewStatsTest {

    @Autowired
    private JacksonTester<ViewStats> json;

    @Test
    public void testViewStats() throws Exception {
        ViewStats viewStats = new ViewStats(
                "192.163.0.1",
                1L);
        JsonContent<ViewStats> result = json.write(viewStats);
        assertThat(result).extractingJsonPathStringValue("$.app").isEqualTo(viewStats.getApp());
        assertThat(result).extractingJsonPathStringValue("$.uri").isEqualTo(viewStats.getUri());
        assertThat(result).extractingJsonPathNumberValue("$.hits").isEqualTo((int) viewStats.getHits());
    }

}