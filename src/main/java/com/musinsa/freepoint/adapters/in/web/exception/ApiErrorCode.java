package com.musinsa.freepoint.adapters.in.web.exception;

import org.springframework.http.HttpStatus;

public enum ApiErrorCode {
    // 도메인 매핑
    INVALID_ACCRUAL_AMOUNT("FP-ACCRUAL-001", HttpStatus.BAD_REQUEST),
    MAX_HOLDING_EXCEEDED("FP-ACCRUAL-002", HttpStatus.BAD_REQUEST),
    EXPIRY_POLICY_VIOLATION("FP-ACCRUAL-003", HttpStatus.BAD_REQUEST),

    // 일반/검증/서버
    BAD_REQUEST("FP-COMMON-400", HttpStatus.BAD_REQUEST),
    VALIDATION_ERROR("FP-COMMON-422", HttpStatus.UNPROCESSABLE_ENTITY),
    INTERNAL_ERROR("FP-COMMON-500", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final HttpStatus status;

    ApiErrorCode(String code, HttpStatus status) {
        this.code = code;
        this.status = status;
    }

    public String code() { return code; }
    public HttpStatus status() { return status; }
}
