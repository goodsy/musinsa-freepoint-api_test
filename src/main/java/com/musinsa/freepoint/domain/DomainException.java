package com.musinsa.freepoint.domain;


import com.musinsa.freepoint.adapters.in.web.exception.ApiErrorCode;

public class DomainException extends RuntimeException {
    private final ApiErrorCode errorCode;

    public DomainException(ApiErrorCode errorCode) {
        super(errorCode.defaultMessage());       // 기본 메시지 사용
        this.errorCode = errorCode;
    }
    public DomainException(ApiErrorCode errorCode, String message) {
        super(message);                           // 오버라이드 메시지
        this.errorCode = errorCode;
    }

    public static DomainException withFormat(ApiErrorCode code, String fmt, Object... args) {
        String base = (code.defaultMessage() == null) ? "" : code.defaultMessage();
        String extra = (fmt == null) ? "" : String.format(fmt, args);
        String msg = extra.isBlank() ? base : base + " " + extra;
        return new DomainException(code, msg.trim());
    }
    public ApiErrorCode errorCode() { return errorCode; }
}
