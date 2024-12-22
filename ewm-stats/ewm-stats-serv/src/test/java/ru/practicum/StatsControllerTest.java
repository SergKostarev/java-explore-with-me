package ru.practicum;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.EndpointHit;
import dto.ViewStats;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.controller.StatsController;
import ru.practicum.service.StatsService;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StatsController.class)
public class StatsControllerTest {

    @Autowired
    ObjectMapper mapper;

    @MockBean
    StatsService statsService;

    @Autowired
    private MockMvc mvc;

    private EndpointHit hit = new EndpointHit("/events/1","192.163.0.1","2022-09-06 11:00:23");
    private ViewStats viewStats1 = new ViewStats("/events/1",1L);
    private ViewStats viewStats2 = new ViewStats("/events/2",3L);

    @Test
    void hit() throws Exception {
        mvc.perform(post("/hit")
                        .content(mapper.writeValueAsString(hit))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void getHits() throws Exception {
        List<ViewStats> viewStatsList = Arrays.asList(viewStats1, viewStats2);
        when(statsService.get(any(), any(), any(), anyBoolean()))
                .thenReturn(viewStatsList);
        mvc.perform(get("/stats")
                        .param("start", "2021-09-06 11:00:23")
                        .param("end", "2022-09-06 11:00:23")
                        .param("uris", "/events/1")
                        .param("unique", "true")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(status().isOk()).andExpect(
                        content().json(mapper.writeValueAsString(viewStatsList)));
    }

}