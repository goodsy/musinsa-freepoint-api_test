package com.musinsa.freepoint.adapters.in.web.interceptor;

import com.musinsa.freepoint.common.util.HmacUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class HeaderValidationInterceptor implements HandlerInterceptor {

    //해당 값은 무신사 무료 포인트 시스템 사용하는 API 정보 별도 관리
    private static final String API_ID = "musinaId";                                        // 실제 값으로 교체
    private static final String API_KEY = "8vYgD+ibnpjKOL770UzCPnI+cX2bQUStvon+ewt00Hw=";   //SHA-256 해시(Base64) — Base64로 인코딩된 해시값

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
        String apiId = request.getHeader("X-MUSINSA-ID");
        String idempotencyKey = request.getHeader("Itempotency-Key");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid Authorization header");
            return false;
        }
        if (apiId == null || !API_ID.equals(apiId)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid X-MUSINSA-ID header");
            return false;
        }
        if ("POST".equalsIgnoreCase(request.getMethod()) && (idempotencyKey == null || idempotencyKey.isEmpty())) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing Itempotency-Key header");
            return false;
        }
        return true;
    }

    // HMAC 서명 검증
    private boolean validateHmacHeader(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String authorization = request.getHeader("Authorization");
        String clientHmac = authorization.replaceFirst("^Bearer\\s+", "");
        String requestData = request.getMethod() + request.getRequestURI();

        if (!HmacUtil.validateHmac(requestData, API_KEY, clientHmac)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "HMAC verification failed");
            return false;
        }
        return true;
    }


}