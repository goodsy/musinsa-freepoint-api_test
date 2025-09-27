package com.musinsa.freepoint.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 단 1개의 Advice로 3가지 경우만 처리:
 * 1) ApiException : 우리가 의도적으로 던진 비즈/정책 예외
 * 2) DataIntegrityViolationException : DB 무결성 충돌을 간단히 409로
 * 3) 그 외 모든 예외 : 500으로 감싸고 correlationId 제공
 *
 * - 로깅 룰: 4xx는 WARN, 5xx는 ERROR
 * - 응답은 ProblemDetail + errorCode + correlationId
 */
@RestControllerAdvice
public class GlobalErrorAdvice {
    private static final Logger log = LoggerFactory.getLogger(GlobalErrorAdvice.class);

    @ExceptionHandler(ApiException.class)
    public ProblemDetail handleApi(ApiException ex, HttpServletRequest req) {
        log.warn("[API-{}] {}", ex.getErrorCode().code(), ex.getMessage());
        var props = new java.util.HashMap<String, Object>();
        if (ex.getProps() != null) props.putAll(ex.getProps());
        putCorrelation(req, props);

        return ProblemDetailsFactory.of(
                ex.getStatus(),
                // title을 간결하게 통일(문서/프론트에 일관된 UX 제공)
                "요청 처리 중 오류가 발생했습니다.",
                ex.getErrorCode(),
                ex.getMessage(),
                props
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleConflict(DataIntegrityViolationException ex, HttpServletRequest req) {
        log.warn("[API-CONFLICT] {}", ex.getMessage());
        var props = new java.util.HashMap<String, Object>();
        putCorrelation(req, props);
        return ProblemDetailsFactory.of(
                HttpStatus.CONFLICT,
                "데이터 충돌이 발생했습니다.",
                ErrorCode.CONFLICT,
                "요청이 현재 데이터 상태와 충돌합니다.",
                props
        );
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleEtc(Exception ex, HttpServletRequest req) {
        log.error("[API-ERROR] {}", ex.getMessage(), ex);
        var props = new java.util.HashMap<String, Object>();
        putCorrelation(req, props);
        props.put("rootCause", ex.getClass().getSimpleName());

        return ProblemDetailsFactory.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "서버 내부 오류가 발생했습니다.",
                ErrorCode.INTERNAL_ERROR,
                "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                props
        );
    }

    private void putCorrelation(HttpServletRequest req, java.util.Map<String, Object> props) {
        Object cid = req.getAttribute(CorrelationFilter.CORRELATION_ID);
        if (cid != null) props.put(CorrelationFilter.CORRELATION_ID, cid.toString());
    }
}
