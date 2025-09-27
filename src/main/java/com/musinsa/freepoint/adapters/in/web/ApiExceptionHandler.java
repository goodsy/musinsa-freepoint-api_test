package com.musinsa.freepoint.adapters.in.web;

import com.musinsa.freepoint.adapters.in.web.exception.ApiErrorCode;
import com.musinsa.freepoint.adapters.in.web.exception.ApiErrorResponse;
import com.musinsa.freepoint.domain.accrual.exception.ExpiryPolicyViolationException;
import com.musinsa.freepoint.domain.accrual.exception.InvalidAccrualAmountException;
import com.musinsa.freepoint.domain.accrual.exception.MaxHoldingExceededException;
import com.musinsa.freepoint.domain.DomainException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ApiExceptionHandler {

    // 4-1) 도메인 예외 → 코드 매핑
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiErrorResponse> handleDomain(DomainException ex, HttpServletRequest req) {
        ApiErrorCode ec = mapDomainCode(ex);
        return ResponseEntity.status(ec.status())
                .body(ApiErrorResponse.of(ec, ex.getMessage(), req.getRequestURI()));
    }

    private ApiErrorCode mapDomainCode(DomainException ex) {
        if (ex instanceof InvalidAccrualAmountException) return ApiErrorCode.INVALID_ACCRUAL_AMOUNT;
        if (ex instanceof MaxHoldingExceededException)   return ApiErrorCode.MAX_HOLDING_EXCEEDED;
        if (ex instanceof ExpiryPolicyViolationException)return ApiErrorCode.EXPIRY_POLICY_VIOLATION;
        return ApiErrorCode.BAD_REQUEST; // 기본값
    }

    // 4-2) 표준 검증 예외
    @ExceptionHandler({ MethodArgumentNotValidException.class, BindException.class })
    public ResponseEntity<ApiErrorResponse> handleBinding(Exception ex, HttpServletRequest req) {
        String msg = "요청 값이 유효하지 않습니다.";
        return ResponseEntity.status(ApiErrorCode.VALIDATION_ERROR.status())
                .body(ApiErrorResponse.of(ApiErrorCode.VALIDATION_ERROR, msg, req.getRequestURI()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraint(ConstraintViolationException ex, HttpServletRequest req) {
        String msg = "파라미터 제약 조건 위반: " + ex.getMessage();
        return ResponseEntity.status(ApiErrorCode.VALIDATION_ERROR.status())
                .body(ApiErrorResponse.of(ApiErrorCode.VALIDATION_ERROR, msg, req.getRequestURI()));
    }

    // 4-3) 그 외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleEtc(Exception ex, HttpServletRequest req) {
        return ResponseEntity.status(ApiErrorCode.INTERNAL_ERROR.status())
                .body(ApiErrorResponse.of(ApiErrorCode.INTERNAL_ERROR, "서버 오류가 발생했습니다.", req.getRequestURI()));
    }
}
