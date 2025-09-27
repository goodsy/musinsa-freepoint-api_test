package com.musinsa.freepoint.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BearerTokenGeneratorTest {

    @Test
    @DisplayName("테스트용 Bearer HMAC 토큰 생성")
    void generateBearerToken() throws Exception {
        String apiKey = "8vYgD+ibnpjKOL770UzCPnI+cX2bQUStvon+ewt00Hw="; // 테스트용 API_KEY
        String method = "POST";
        String uri = "/api/point/accruals";
        String requestData = method + uri;

        String hmac = HmacUtil.generateHmac(requestData, apiKey);
        String bearerToken = "Bearer " + hmac;

        System.out.println("Bearer Token: " + bearerToken);
    }
}
