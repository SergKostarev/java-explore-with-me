package ru.practicum;

import dto.ViewStats;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.handler.IncorrectDataException;
import ru.practicum.service.StatsService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class StatsServiceTest {

    private final StatsService service;

    @Test
    void getUniqueHits() {
        List<ViewStats> statList = service.get("2022-09-01 11:00:23", "2022-09-30 11:00:23", new String[]{"/events/1"}, true);
        Assertions.assertThat(statList).hasSize(1);
        ViewStats stat = statList.get(0);
        Assertions.assertThat(stat.getHits()).isEqualTo(2L);
        Assertions.assertThat(stat.getUri()).isEqualTo("/events/1");
        Assertions.assertThat(stat.getApp()).isEqualTo("ewm-main-service");
    }

    @Test
    void getNotUniqueHits() {
        List<ViewStats> statList = service.get("2022-09-01 11:00:23", "2022-09-30 11:00:23", new String[]{"/events/1"}, false);
        Assertions.assertThat(statList).hasSize(1);
        ViewStats stat = statList.get(0);
        Assertions.assertThat(stat.getHits()).isEqualTo(3L);
        Assertions.assertThat(stat.getUri()).isEqualTo("/events/1");
        Assertions.assertThat(stat.getApp()).isEqualTo("ewm-main-service");
    }

    @Test
    void givenWrongStartDateFormatShouldThrowIncorrectDataException() {
        assertThatThrownBy(() -> {
            service.get("2022-09-01 1:00:23", "2022-09-30 11:00:23", new String[]{"/events/1"}, false);
        }).isInstanceOf(IncorrectDataException.class);
    }

    @Test
    void givenStartDateBeforeEndDateShouldThrowIncorrectDataException() {
        assertThatThrownBy(() -> {
            service.get("2022-09-30 11:00:23", "2022-09-01 11:00:23", new String[]{"/events/1"}, false);
        }).isInstanceOf(IncorrectDataException.class);
    }

}