package com.musinsa.freepoint.adapters.in.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccrualControllerTest.class)
class IdempotencyAspectTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("멱등키 없으면 400 반환")
    void noIdempotencyKey_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/points/accruals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"u1\",\"amount\":1000,\"sourceType\":\"ORDER\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("이미 처리된 멱등키면 409 반환")
    void duplicatedIdempotencyKey_returns409() throws Exception {
        String key = "DUPLICATE-KEY";
        // 첫 요청: 정상 처리
        mockMvc.perform(post("/api/v1/points/accruals")
                        .header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"u1\",\"amount\":1000,\"sourceType\":\"ORDER\"}"))
                .andExpect(status().isOk());

        // 두 번째 요청: 409 반환
        mockMvc.perform(post("/api/v1/points/accruals")
                        .header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"u1\",\"amount\":1000,\"sourceType\":\"ORDER\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("신규 멱등키면 정상 처리")
    void newIdempotencyKey_returns200() throws Exception {
        mockMvc.perform(post("/api/v1/points/accruals")
                        .header("Idempotency-Key", "NEW-KEY-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"u1\",\"amount\":1000,\"sourceType\":\"ORDER\"}"))
                .andExpect(status().isOk());
    }
}
