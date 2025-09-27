package com.musinsa.freepoint.common.exception;

/**
 * 간단하지만 확장 가능한 에러 코드 규격.
 * - 문자열 코드: 로그/모니터링에서 키로 사용
 * - 설명(description): 운영자/문서용
 */
public enum ErrorCode {
    // Common
    INVALID_REQUEST("C001", "잘못된 요청입니다"),
    UNAUTHORIZED("C002", "인증이 필요합니다"),
    FORBIDDEN("C003", "권한이 없습니다"),
    NOT_FOUND("C004", "대상을 찾을 수 없습니다"),
    CONFLICT("C005", "리소스 충돌이 발생했습니다"),
    INTERNAL_ERROR("C999", "서버 내부 오류입니다"),

    // Business
    NO_EXISIT_ACCRUAL("B001", "적립 내역이 존재하지 않습니다."),
    MAX_BALANCE_EXCEEDED("B001", "보유 한도를 초과했습니다"),
    IDEMPOTENCY_VIOLATION("B002", "멱등성 위반입니다");

    private final String code;
    private final String description;

    ErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String code() { return code; }
    public String description() { return description; }
}
