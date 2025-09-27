package com.musinsa.freepoint.common.exception;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.UUID;

/**
 * 초간단 상관ID 필터.
 * - 요청별 correlationId 생성 후 MDC에 넣어 로깅과 에러응답에 함께 실어줌.
 * - 운영 추적성 강화(차별화 포인트).
 */
public class CorrelationFilter implements Filter {
    public static final String CORRELATION_ID = "correlationId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String id = UUID.randomUUID().toString();
        try {
            MDC.put(CORRELATION_ID, id);
            request.setAttribute(CORRELATION_ID, id);
            chain.doFilter(request, response);
        } finally {
            MDC.remove(CORRELATION_ID);
        }
    }
}
