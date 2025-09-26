package com.musinsa.freepoint.adapters.in.web.interceptor;

import com.musinsa.freepoint.common.util.HmacUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;


class ApiHeaderValidationInterceptorTest {

    private final HeaderValidationInterceptor interceptor = new HeaderValidationInterceptor();


    private String API_KEY;
    private String API_ID;
    private String method;
    private String uri;

    @BeforeEach
    void setUp() {
        API_KEY = "8vYgD+ibnpjKOL770UzCPnI+cX2bQUStvon+ewt00Hw=";
        API_ID = "musinsaId";
        method = "POST";
        uri = "/api/test";
    }

    @Test
    void hmac_정상_검증_성공() throws Exception {

        String requestData = method + uri;
        String hmac = HmacUtil.generateHmac(requestData, API_KEY);

        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.addHeader("Authorization", "Bearer " + hmac);
        request.addHeader("X-MUSINSA-ID", API_ID);
        request.addHeader("Itempotency-Key", "test-key");

        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());
        assertThat(result).isTrue();
    }

    @Test
    void hmac_불일치_실패() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/test");
        request.addHeader("Authorization", "Bearer invalidhmac");
        request.addHeader("X-MUSINSA-ID", API_ID);
        request.addHeader("Itempotency-Key", "test-key");

        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());
        assertThat(result).isFalse();
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void 필수_헤더_누락_실패() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/test");
        // Authorization 헤더 누락
        request.addHeader("X-MUSINSA-ID", API_ID);
        request.addHeader("Itempotency-Key", "test-key");

        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());
        assertThat(result).isFalse();
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
    }
}