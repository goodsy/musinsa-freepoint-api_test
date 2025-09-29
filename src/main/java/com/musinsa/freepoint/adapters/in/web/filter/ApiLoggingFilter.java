
package com.musinsa.freepoint.adapters.in.web.filter;

import com.musinsa.freepoint.adapters.in.web.ApiHeaderConstants;
import com.musinsa.freepoint.adapters.out.logsink.ApiLogSink;
import com.musinsa.freepoint.adapters.out.persistence.ApiLogRepository;
import com.musinsa.freepoint.domain.KeyGenerator;
import com.musinsa.freepoint.domain.log.ApiLog;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

public class ApiLoggingFilter extends OncePerRequestFilter {

    private static final Set<String> MASK_HEADERS = Set.of("authorization", "");
    private static final int MAX_BODY = 10_000;

    private final ApiLogRepository repository;
    private final ApiLogSink sink;

    public ApiLoggingFilter(ApiLogRepository repository, ApiLogSink sink) {
        this.repository = repository;
        this.sink = sink;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String apiLogId = Optional.ofNullable(req.getHeader("X-Request-Id"))
                .filter(s -> !s.isBlank())
                .orElse(KeyGenerator.generateApiLogId());

        MDC.put("rid", apiLogId);

        String uri = req.getRequestURI();
        if (uri.startsWith("/swagger-ui") || uri.startsWith("/v3/api-docs") || uri.startsWith("/h2-console")) {
            chain.doFilter(req, res);
            return;
        }

        long start = System.currentTimeMillis();
        ContentCachingRequestWrapper request = new ContentCachingRequestWrapper(req);
        ContentCachingResponseWrapper response = new ContentCachingResponseWrapper(res);


        try {
            chain.doFilter(request, response);
        } finally {
            long took = System.currentTimeMillis() - start;
            Map<String, String> headers = collectHeaders(request);
            String reqBody = bodySample(request.getContentAsByteArray());
            String respBody = bodySample(response.getContentAsByteArray());


           sink.enqueue(new ApiLogSink.ApiLog(apiLogId, request.getMethod(), request.getRequestURI(),
                    headers, reqBody, response.getStatus(), respBody, took));

            String idempotencyKey = request.getHeader(ApiHeaderConstants.IDEMPOTENCY_KEY);
            ApiLog log = ApiLog.builder()
                    .logId(apiLogId)
                    .apiMethod(request.getMethod())
                    .apiUri(request.getRequestURI())
                    .idempotencyKey(idempotencyKey)
                    .requestHeaders(headers.toString())
                    .requestBody(reqBody)
                    //.responseHeaders(res.getHeader(HttpHeaders.CONTENT_TYPE))
                    .responseBody(respBody)
                    .statusCode(String.valueOf(res.getStatus()))
                    //.durationMs(System.currentTimeMillis()-start)
                    //.clientIp(RequestUtils.clientIp(request))
                    .createdAt(LocalDateTime.now())
                    .build();
            repository.save(log);

            response.copyBodyToResponse();
            MDC.remove("rid");
        }
    }

    private static Map<String, String> collectHeaders(HttpServletRequest req) {
        Map<String, String> map = new LinkedHashMap<>();
        Enumeration<String> names = req.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            String lower = name.toLowerCase(Locale.ROOT);
            String value = req.getHeader(name);
            if (MASK_HEADERS.contains(lower) && value != null) {
                map.put(name, mask(value));
            } else {
                map.put(name, value);
            }
        }
        return map;
    }

    private static String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        return content.length > 0 ? new String(content, StandardCharsets.UTF_8) : "{}";
    }

    private static String mask(String v) {
        if (v == null || v.length() < 8) return "****";
        return v.substring(0, 6) + "...(masked)";
    }

    private static String bodySample(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return "";
        String s = new String(bytes, StandardCharsets.UTF_8);
        return s.length() > MAX_BODY ? s.substring(0, MAX_BODY) + "...(truncated)" : s;
    }
}
