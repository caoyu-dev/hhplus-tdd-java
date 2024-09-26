package io.hhplus.tdd.point;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PointControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private HistoryRepository historyRepository;

    @BeforeEach
    void setUp() {
    }

    @Test
    void test_get_point_success() throws Exception {
        // Given
        long userId = 1L;
        long point = 500L;
        pointRepository.addPoint(userId, point);

        // When & Then
        mockMvc.perform(get("/point/{id}", userId))
                .andExpect(jsonPath("$.amount").value(point));
    }

    @Test
    void test_get_point_userNotFound() throws Exception {
        // Given
        long userId = 999L;

        // When & Then
        mockMvc.perform(get("/point/{id}", userId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("해당 유저(" + userId + ")는 존재하지 않습니다."));
    }

    @Test
    void test_charge_point_success() throws Exception {
        // Given
        long userId = 1L;
        long point = 100L;
        pointRepository.addPoint(userId, point);
        String chargeRequest = "{\"amount\":50}";

        // When & Then
        mockMvc.perform(patch("/point/{id}/charge", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(chargeRequest))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":1,\"point\":150}"));
    }

    @Test
    void test_charge_point_userNotFound() throws Exception {
        long userId = 999L;
        String chargeRequest = "{\"amount\":50}";

        mockMvc.perform(patch("/point/{id}/charge", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(chargeRequest))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":999,\"point\":50}"));
    }

    @Test
    void test_charge_point_overLimit() throws Exception {
        // Given
        long userId = 1L;
        long point = 49000L;
        pointRepository.addPoint(userId, point);
        String chargeRequest = "{\"amount\":2000}";

        // When & Then
        mockMvc.perform(patch("/point/{id}/charge", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(chargeRequest))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertEquals("최대 50000 포인트까지 모을 수 있습니다.", result.getResolvedException().getMessage()));
    }

    @Test
    void test_use_point_success() throws Exception {
        // Given
        long userId = 1L;
        pointRepository.addPoint(userId, 200L);
        String useRequest = "{\"amount\":50}";

        // When & Then
        mockMvc.perform(patch("/point/{id}/use", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(useRequest))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":1,\"point\":150}"));
    }

    @Test
    void test_use_point_insufficientBalance() throws Exception {
        long userId = 1L;
        long point = 50L;
        pointRepository.addPoint(userId, point);
        String useRequest = "{\"amount\":100}";

        mockMvc.perform(patch("/point/{id}/use", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(useRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("사용할 포인트가 충분하지 않습니다."));
    }

    @Test
    void test_get_point_history_success() throws Exception {
        // Given
        long userId = 1L;
        historyRepository.addHistory(userId, 100L, TransactionType.CHARGE, 1L);
        historyRepository.addHistory(userId, 50L, TransactionType.USE, 1L);

        // When & Then
        mockMvc.perform(get("/point/{id}/histories", userId))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"userId\":1,\"amount\":100,\"type\":\"CHARGE\",\"updateMillis\":1},{\"userId\":1,\"amount\":50,\"type\":\"USE\"}]"));
    }

    @Test
    void test_get_point_history_noHistory() throws Exception {
        long userId = 1L;

        mockMvc.perform(get("/point/{id}/histories", userId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("해당 유저(" + userId + ")의 트랜잭션 내역이 없습니다."));
    }
}
