package com.musinsa.freepoint.adapters.in.web.filter;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.musinsa.freepoint.adapters.in.web.ApiHeaderConstants;
import com.musinsa.freepoint.application.service.IdempotencyService;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class ApiLogFilter extends OncePerRequestFilter {

    private final IdempotencyService idempotencyService;

    public ApiLogFilter(IdempotencyService idempotencyService) {
        this.idempotencyService = idempotencyService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        if (request.getDispatcherType() != DispatcherType.REQUEST) {
            chain.doFilter(request, response);
            return;
        }

        String uri = request.getRequestURI();
        if (uri.startsWith("/swagger-ui") || uri.startsWith("/v3/api-docs") || uri.startsWith("/actuator")) {
            chain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper reqW = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper resW = new ContentCachingResponseWrapper(response);

        String idemKey = reqW.getHeader(ApiHeaderConstants.IDEMPOTENCY_KEY);
        String headers = headersString(reqW);
        String reqBody = getRequestBody(reqW);

        // acquireOrCached: 있으면 캐시 응답, 없으면 최초 insert
        var cached = StringUtils.hasText(idemKey)
                ? idempotencyService.acquireOrCached(idemKey, reqW.getMethod(), reqW.getRequestURI(), headers, reqBody)
                : java.util.Optional.<IdempotencyService.CachedResponse>empty();

        if (cached.isPresent()) {
            var cr = cached.get();
            resW.setStatus(cr.statusCode());
            resW.setContentType("application/json");
            if (cr.body() != null) resW.getWriter().write(cr.body());
            resW.copyBodyToResponse();
            return;
        }

        try {
            chain.doFilter(reqW, resW);
        } finally {
            String respBody = new String(resW.getContentAsByteArray(), StandardCharsets.UTF_8);

            if (StringUtils.hasText(idemKey)) {
                idempotencyService.complete(idemKey, resW.getStatus(), respBody);
            } else {
                // 멱등키 없더라도 api_log에 넣고 싶다면 별도 ApiLogUseCase로 분리해서 처리
            }
            resW.copyBodyToResponse();
        }
    }

    private static String headersString(HttpServletRequest request) {
        List<String> targetHeaders = List.of(ApiHeaderConstants.HEADER_MUSINSA_ID, ApiHeaderConstants.IDEMPOTENCY_KEY, ApiHeaderConstants.DEFAULT_CONTENT_TYPE_VALUE);
        StringBuilder headers = new StringBuilder();
        Map<String, String> headersMap = new HashMap<>();

        for (String name : targetHeaders) {
            String value = request.getHeader(name);
            if (value != null) {
                headersMap.put(name, value);
                headers.append(name).append(": ").append(value).append(",");
            }
        }
        try {
            return new ObjectMapper().writeValueAsString(headersMap);
        } catch (JsonProcessingException e) {
            return "{}"; // JSON 변환 오류 시 빈 JSON 반환
        }
        //return headers.toString();
    }

    private static String getHeaders(HttpServletRequest request) {
        Map<String, String> headersMap = new HashMap<>();
        request.getHeaderNames().asIterator().forEachRemaining(header -> {
            if ("Request-ID".contains(header)) {
                headersMap.put(header, request.getHeader(header));
            }
        });

        try {
            return new ObjectMapper().writeValueAsString(headersMap);
        } catch (JsonProcessingException e) {
            return "{}"; // JSON 변환 오류 시 빈 JSON 반환
        }
    }

    private static String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        return content.length > 0 ? new String(content, StandardCharsets.UTF_8) : "{}";
    }
}

