package com.musinsa.freepoint.adapters.in.web;

import org.springframework.http.MediaType;

import java.util.List;

/**
 * <p><b>ApiHeaderConstants</b></p>
 *
 * <p>API 요청 및 응답에서 공통적으로 사용되는 HTTP 헤더 상수를 정의하는 클래스.</p>

 * @version 1.0
 */
public final class ApiHeaderConstants {

    public static final String HEADER_AUTHORIZATION_PREFIX = "Bearer ";
    public static final String HEADER_MUSINSA_ID = "X-MUSINSA-ID";
    public static final String IDEMPOTENCY_KEY = "Idempotency-Key";

    public static final String DEFAULT_CONTENT_TYPE_VALUE = MediaType.APPLICATION_JSON_VALUE;
    public static final String X_REAL_IP = "X-Real-IP";
    public static final String X_FORWARDED_FOR = "X-Forwarded-For";

    public static final List<MediaType> ALLOWED_MEDIA_TYPES = List.of(
            MediaType.APPLICATION_JSON,
            MediaType.APPLICATION_XML,
            MediaType.TEXT_XML,
            MediaType.TEXT_PLAIN);

    private ApiHeaderConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}