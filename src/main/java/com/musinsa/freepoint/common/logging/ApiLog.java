package com.musinsa.freepoint.common.logging;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "api_log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiLog {
    /** 로그 PK (자동 증가) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    /** 요청 고유 식별자 */
    @Column(name = "request_id", length = 64)
    private String requestId;

    /** api id */
    @Column(name = "api_id", length = 10, nullable = false)
    private String apiId;

    /** API HTTP 메서드 (GET, POST 등) */
    @Column(name = "api_method", length = 10, nullable = false)
    private String apiMethod;

    /** API 엔드포인트 URL */
    @Column(name = "api_url", length = 128, nullable = false)
    private String apiUrl;

    /** 멱등성 키 */
    @Column(name = "idempotency_key", length = 200)
    private String idempotencyKey;

    /** 주문 번호 */
    @Column(name = "order_no", length = 64)
    private String orderNo;

    /** 요청 본문 해시값 */
    @Column(name = "request_hash", length = 64, nullable = false)
    private String requestHash;

    /** 처리 상태 코드 */
    @Column(name = "status_code", length = 5, nullable = false)
    private String statusCode;

    /** 요청 헤더 */
    @Lob
    @Column(name = "req_headers", nullable = false)
    private String reqHeaders;

    /** 요청 본문 */
    @Lob
    @Column(name = "req_body", nullable = false)
    private String reqBody;

    /** 응답 본문 */
    @Lob
    @Column(name = "res_body", nullable = false)
    private String resBody;

    /** 생성 일시 */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public static ApiLog of(String key) {
        ApiLog entity = new ApiLog();
        entity.idempotencyKey = key;
        return entity;
    }
}