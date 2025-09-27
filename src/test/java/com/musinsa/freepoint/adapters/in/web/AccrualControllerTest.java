package com.musinsa.freepoint.adapters.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musinsa.freepoint.adapters.in.web.dto.AccrualRequest;
import com.musinsa.freepoint.adapters.out.persistence.PointAccrualRepository;
import com.musinsa.freepoint.common.util.HmacUtil;
import com.musinsa.freepoint.domain.accrual.PointAccrual;
import com.musinsa.freepoint.domain.accural.AccrualRequestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AccrualControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    PointAccrualRepository accrualRepository;

    @Autowired
    ObjectMapper objectMapper;

    private String API_KEY;
    private String API_ID;
    private String method;
    private String URI;

    @BeforeEach
    void setUp() {
        API_KEY = "8vYgD+ibnpjKOL770UzCPnI+cX2bQUStvon+ewt00Hw=";
        API_ID = "musinsaId";
        method = "POST";
        URI = "/api/v1/point/accruals";
    }

    @Test
    @Rollback
    void accruePoint_success_and_persisted_in_db() throws Exception {
        // given
        String USER_ID = "user100";
        long amount = 5000L;
        int expiryDays = 365;
        String sourceType = "ORDER";
        String sourceId = "order-123";
        boolean manual = false;


        AccrualRequest accrualRequest = AccrualRequestFactory.create(USER_ID, amount, expiryDays, manual, sourceType, sourceId);

        String hmac = HmacUtil.generateHmac("POST"+URI, API_KEY);
        // when
        mockMvc.perform(post(URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", ApiHeaderConstants.HEADER_AUTHORIZATION_PREFIX + hmac)
                        .header(ApiHeaderConstants.HEADER_MUSINSA_ID, API_ID)
                        .header(ApiHeaderConstants.IDEMPOTENCY_KEY, "test-key")
                        .content(objectMapper.writeValueAsString(accrualRequest)))
                .andExpect(status().isOk());

        // then
        List<PointAccrual> accruals = accrualRepository.findUsableByUserId(USER_ID);
        assertThat(accruals).isNotEmpty();
        PointAccrual accrual = accruals.get(0);
        assertThat(accrual.getUserId()).isEqualTo(USER_ID);
        assertThat(accrual.getAmount()).isEqualTo(amount);
        assertThat(accrual.getRemainAmount()).isEqualTo(amount);
        assertThat(accrual.getSourceType()).isEqualTo(sourceType);
        assertThat(accrual.getSourceId()).isEqualTo(sourceId);
        assertThat(accrual.isManual()).isEqualTo(manual);
        assertThat(accrual.getExpireAt()).isAfter(LocalDateTime.now());
    }

}