package com.musinsa.freepoint.adapters.in.web.exception;

import org.springframework.http.HttpStatus;

/*

1. Prefix
- FP-COMMON-* : 공통 오류(형식/서버)
- FP-REQ-* : 요청 값/형식
- FP-AUTH-* : 인증/인가
- FP-IDEMP-* : 멱등성/중복
- FP-ACCRUAL-*, FP-USAGE-*, FP-WALLET-* : 도메인별
- FP-DB-*, FP-EXT-* : 인프라/외부

2. 번호 부여
- 영역별로 001부터 순차 증가
- 의미 있는 단위로 건너뛰기 허용 (ex. *010은 토큰 관련)

3. 메시지
- defaultMessage는 친절한 한글
- 예외에 ex.getMessage()가 있으면 우선 사용, 없으면 defaultMessage() 사용
 */

public enum ApiErrorCode {

    /* ===== 공통/요청 형식/검증 ===== */
    BAD_REQUEST                 ("FP-COMMON-400", HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    VALIDATION_ERROR           ("FP-COMMON-422", HttpStatus.UNPROCESSABLE_ENTITY, "요청 값이 유효하지 않습니다."),
    INTERNAL_ERROR             ("FP-COMMON-500", HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),


    // 세부: 요청 파라미터/바디/타입/누락 등 (컨트롤러 검증)
    REQ_BODY_INVALID           ("FP-REQ-001", HttpStatus.BAD_REQUEST, "요청 본문이 유효하지 않습니다."),
    REQ_PARAM_VIOLATION        ("FP-REQ-002", HttpStatus.BAD_REQUEST, "요청 파라미터가 유효하지 않습니다."),
    REQ_BIND_OR_TYPE_MISMATCH  ("FP-REQ-003", HttpStatus.BAD_REQUEST, "요청 파라미터 타입이 올바르지 않습니다."),
    REQ_JSON_MALFORMED         ("FP-REQ-004", HttpStatus.BAD_REQUEST, "JSON 형식이 올바르지 않거나 타입이 맞지 않습니다."),
    REQ_PARAM_MISSING          ("FP-REQ-005", HttpStatus.BAD_REQUEST, "필수 요청 값이 누락되었습니다."),

    /* ===== 멱등성/중복 ===== */
    IDEMPOTENCY_CONFLICT       ("FP-IDEMP-409", HttpStatus.CONFLICT, "이미 처리된 요청입니다."),
    DUPLICATE_REQUEST          ("FP-COMMON-409", HttpStatus.CONFLICT, "중복 요청입니다."),

    /* ===== 인증/인가 ===== */
    HEADER_MISSING_AUTH("FP-HEADER-401", HttpStatus.UNAUTHORIZED, "Authorization 헤더 누락 또는 유효하지 않는 값입니다."),
    HEADER_MISSING_MUSINSA_ID("FP-HEADER-402", HttpStatus.BAD_REQUEST, "X-MUSINSA-ID 헤더 누락 또는 유효하지 않는 값입니다."),
    HEADER_MISSING_IDEMPOTENCY_KEY("FP-HEADER-403", HttpStatus.BAD_REQUEST, "Idempotency-Key 헤더 누락 또는 유효하지 않는 값입니다."),

    AUTH_UNAUTHORIZED          ("FP-AUTH-401", HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    AUTH_FORBIDDEN             ("FP-AUTH-402", HttpStatus.FORBIDDEN, "요청 권한이 없습니다."),
    AUTH_BEARER_UNAUTHORIZED   ("FP-AUTH-403", HttpStatus.UNAUTHORIZED, "Bearer 토큰값을 확인해주세요."),


    /* ===== 도메인: 적립(ACCRUAL) ===== */
    INVALID_ACCRUAL_AMOUNT     ("FP-ACCRUAL-001", HttpStatus.BAD_REQUEST, "요청한 가능한 적립 금액이 아닙니다."),
    MAX_HOLDING_EXCEEDED       ("FP-ACCRUAL-002", HttpStatus.BAD_REQUEST, "개인 최대 보유 한도를 초과했습니다."),
    EXPIRY_POLICY_VIOLATION    ("FP-ACCRUAL-003", HttpStatus.BAD_REQUEST, "만료 정책을 위반했습니다."),
    ACCRUAL_NOT_FOUND       ("FP-ACCRUAL-404", HttpStatus.NOT_FOUND,  "적립 내역을 찾을 수 없습니다."),
    ACCRUAL_NOT_OWNED       ("FP-ACCRUAL-403", HttpStatus.FORBIDDEN,   "본인 적립만 취소할 수 있습니다."),
    CANCEL_AMOUNT_EXCEEDS   ("FP-ACCRUAL-004", HttpStatus.BAD_REQUEST, "적립 금액보다 큰 금액은 취소할 수 없습니다."),
    ACCRUAL_INACTIVE        ("FP-ACCRUAL-005", HttpStatus.BAD_REQUEST, "적립 취소가 불가능한 건입니다."),


    /* ===== 도메인: 사용/취소(USAGE) ===== */
    INSUFFICIENT_BALANCE       ("FP-USAGE-001", HttpStatus.BAD_REQUEST, "보유한 잔액 포인트가 부족합니다."),
    USED_POINT_CANCELLATION_NOT_ALLOWED       ("FP-USAGE-002", HttpStatus.NOT_FOUND, "이미 사용된 적립 포인트는 취소가 불가능합니다."),
    USAGE_ALREADY_CANCELED     ("FP-USAGE-003", HttpStatus.CONFLICT, "이미 취소된 사용 내역입니다."),
    POINT_BALANCE_INSUFFICIENT ("FP-USAGE-004", HttpStatus.CONFLICT, "포인트 잔액 부족합니다."),
    POINT_USAGE_HISTORY_NOT_FOUND ("FP-USAGE-005", HttpStatus.CONFLICT, "포인트 사용 내역이 존재하지 않습니다."),
    CANCEL_AMOUNT_EXCEEDS_ORIGINAL_USAGE ("FP-USAGE-006", HttpStatus.CONFLICT, "취소 금액이 원본 사용 금액을 초과합니다."),
    DUPLICATE_USAGE_HISTORY  ("FP-USAGE-007", HttpStatus.CONFLICT, "중복된 사용 내역이 존재합니다."),
    /* ===== 지갑/사용자 ===== */
    WALLET_NOT_FOUND           ("FP-WALLET-001", HttpStatus.NOT_FOUND, "사용자 지갑이 존재하지 않습니다."),
    USER_NOT_FOUND             ("FP-USER-001",   HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),


    // 외부 연동/시스템
    EXTERNAL_API_ERROR         ("FP-EXT-502", HttpStatus.BAD_GATEWAY, "외부 시스템 연동에 실패했습니다."),
    RATE_LIMIT_EXCEEDED        ("FP-EXT-429", HttpStatus.TOO_MANY_REQUESTS, "요청이 너무 많습니다."),
    SERVICE_UNAVAILABLE        ("FP-EXT-503", HttpStatus.SERVICE_UNAVAILABLE, "일시적으로 서비스를 사용할 수 없습니다."),

    // 데이터/DB
    DB_CONSTRAINT_VIOLATION    ("FP-DB-409", HttpStatus.CONFLICT, "데이터 제약 조건을 위반했습니다."),
    DB_ACCESS_ERROR            ("FP-DB-500", HttpStatus.INTERNAL_SERVER_ERROR, "데이터 처리 중 오류가 발생했습니다.");


    private final String code;
    private final HttpStatus status;
    private final String defaultMessage;

    ApiErrorCode(String code, HttpStatus status, String defaultMessage) {
        this.code = code;
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public String code() { return code; }
    public HttpStatus status() { return status; }
    public String defaultMessage() { return defaultMessage; }
}
