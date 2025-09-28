package com.musinsa.freepoint.adapters.in.web.interceptor;

import com.musinsa.freepoint.adapters.in.web.ApiHeaderConstants;
import com.musinsa.freepoint.adapters.in.web.ApiResponse;
import com.musinsa.freepoint.adapters.in.web.exception.ApiErrorCode;
import com.musinsa.freepoint.common.util.HmacUtil;
import com.musinsa.freepoint.config.ApiTestConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class HeaderValidationInterceptor implements HandlerInterceptor {


    private final ApiTestConfig config;

    public HeaderValidationInterceptor(ApiTestConfig config) {
        this.config = config;
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //Header 필수값 체크
        if (!checkRequiredHeaders(request, response)) {
            return false;
        }
        
        //Header HMAC 서명 검증
        if (!validateHmacHeader(request, response)) {
            return false;
        }
        return true;
    }

    // 필수 헤더값 체크
    private boolean checkRequiredHeaders(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String authorization = request.getHeader("Authorization");
        String apiId = request.getHeader(ApiHeaderConstants.HEADER_MUSINSA_ID);
        String idempotencyKey = request.getHeader(ApiHeaderConstants.IDEMPOTENCY_KEY);

        log.info("config.getMusinsaId(): {}", config.getMusinsaId());


        if (authorization == null || !authorization.startsWith(ApiHeaderConstants.HEADER_AUTHORIZATION_PREFIX)) {
            ApiResponse.filterSendError(response, HttpServletResponse.SC_BAD_REQUEST, ApiErrorCode.HEADER_MISSING_AUTH);
            return false;
        }
        if (apiId == null || !config.getMusinsaId().equals(apiId)) {
            ApiResponse.filterSendError(response, HttpServletResponse.SC_BAD_REQUEST, ApiErrorCode.HEADER_MISSING_MUSINSA_ID);
            return false;
        }
        if ("POST".equalsIgnoreCase(request.getMethod()) && (idempotencyKey == null || idempotencyKey.isEmpty())) {
            ApiResponse.filterSendError(response, HttpServletResponse.SC_BAD_REQUEST, ApiErrorCode.HEADER_MISSING_IDEMPOTENCY_KEY);
            return false;
        }
        return true;
    }

    // HMAC 서명 검증
    private boolean validateHmacHeader(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String authorization = request.getHeader("Authorization");
        String clientHmac = authorization.replaceFirst("^Bearer\\s+", "");
        String requestData = request.getMethod() + request.getRequestURI();
        log.info("requestData: {}", requestData);
        log.info("clientHmac: {}", clientHmac);
        if (!HmacUtil.validateHmac(requestData, config.getApiKey(), clientHmac)) {
            ApiResponse.filterSendError(response, HttpServletResponse.SC_BAD_REQUEST, ApiErrorCode.AUTH_BEARER_UNAUTHORIZED);
            return false;
        }
        return true;
    }


}