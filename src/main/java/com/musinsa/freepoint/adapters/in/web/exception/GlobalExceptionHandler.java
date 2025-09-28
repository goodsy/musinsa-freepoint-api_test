package com.musinsa.freepoint.adapters.in.web.exception;

import com.musinsa.freepoint.adapters.in.web.ApiResponse;
import com.musinsa.freepoint.domain.DomainException;


import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.SocketTimeoutException;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "com.musinsa.freepoint")
public class GlobalExceptionHandler {

    /* ========= 도메인 계층 예외 ========= */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomain(DomainException ex) {
        var ec  = ex.errorCode();
        var msg = (ex.getMessage() == null || ex.getMessage().isBlank())
                ? ec.defaultMessage()
                : ex.getMessage();
        return ResponseEntity.status(ec.status())
                .body(ApiResponse.error(ec.code(), msg));
    }

    /* ========= 요청 검증/형식 ========= */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        var details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> ApiResponse.FieldError.of(fe.getField(), fe.getDefaultMessage(), fe.getRejectedValue()))
                .toList();

        var first = details.isEmpty() ? null : details.get(0);
        String msg = (first == null)
                ? defaultMessageOr(ApiErrorCode.REQ_BODY_INVALID, "Invalid request")
                : first.field() + " " + first.reason();

        log.warn("[400] MethodArgumentNotValid: {}", msg);
        return ResponseEntity
                .status(ApiErrorCode.REQ_BODY_INVALID.status())
                .body(ApiResponse.error(ApiErrorCode.REQ_BODY_INVALID.code(), msg));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        var details = ex.getConstraintViolations().stream()
                .map(v -> ApiResponse.FieldError.of(v.getPropertyPath().toString(), v.getMessage(), v.getInvalidValue()))
                .toList();

        String msg = details.isEmpty()
                ? defaultMessageOr(ApiErrorCode.REQ_PARAM_VIOLATION, "Invalid request")
                : details.get(0).field() + " " + details.get(0).reason();

        log.warn("[400] ConstraintViolation: {}", msg);
        return ResponseEntity
                .status(ApiErrorCode.REQ_PARAM_VIOLATION.status())
                .body(ApiResponse.error(ApiErrorCode.REQ_PARAM_VIOLATION.code(), msg));
    }

    @ExceptionHandler({BindException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiResponse<Void>> handleBind(Exception ex) {
        log.warn("[400] Bind/TypeMismatch: {}", ex.getMessage());
        return ResponseEntity
                .status(ApiErrorCode.REQ_BIND_OR_TYPE_MISMATCH.status())
                .body(ApiResponse.error(ApiErrorCode.REQ_BIND_OR_TYPE_MISMATCH.code(),
                        defaultMessageOr(ApiErrorCode.REQ_BIND_OR_TYPE_MISMATCH, "Invalid parameter or type")));
    }

/*    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException ex) {
        log.warn("[400] NotReadable: {}", ex.getMessage());
        return ResponseEntity
                .status(ApiErrorCode.REQ_JSON_MALFORMED.status())
                .body(ApiResponse.error(ApiErrorCode.REQ_JSON_MALFORMED.code(),
                        defaultMessageOr(ApiErrorCode.REQ_JSON_MALFORMED, "Malformed JSON or wrong type")));
    }*/

    @ExceptionHandler({MissingServletRequestParameterException.class, MissingPathVariableException.class})
    public ResponseEntity<ApiResponse<Void>> handleMissingParam(Exception ex) {
        String msg;
        if (ex instanceof MissingServletRequestParameterException m) {
            msg = "Missing parameter: " + m.getParameterName();
        } else if (ex instanceof MissingPathVariableException m) {
            msg = "Missing path variable: " + m.getVariableName();
        } else {
            msg = defaultMessageOr(ApiErrorCode.REQ_PARAM_MISSING, "Missing required parameter");
        }
        log.warn("[400] Missing: {}", msg);
        return ResponseEntity
                .status(ApiErrorCode.REQ_PARAM_MISSING.status())
                .body(ApiResponse.error(ApiErrorCode.REQ_PARAM_MISSING.code(), msg));
    }

    /* ========= 인증/인가 ========= */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuth(AuthenticationException ex) {
        log.warn("[401] Auth: {}", ex.getMessage());
        return ResponseEntity
                .status(ApiErrorCode.AUTH_UNAUTHORIZED.status())
                .body(ApiResponse.error(ApiErrorCode.AUTH_UNAUTHORIZED.code(),
                        defaultMessageOr(ApiErrorCode.AUTH_UNAUTHORIZED, "Unauthorized")));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("[403] Forbidden: {}", ex.getMessage());
        return ResponseEntity
                .status(ApiErrorCode.AUTH_FORBIDDEN.status())
                .body(ApiResponse.error(ApiErrorCode.AUTH_FORBIDDEN.code(),
                        defaultMessageOr(ApiErrorCode.AUTH_FORBIDDEN, "Forbidden")));
    }

    // (선택) JWT 커스텀 예외가 있으면 여기에 매핑
    // @ExceptionHandler(JwtTokenInvalidException.class) ...
    // @ExceptionHandler(JwtTokenExpiredException.class) ...

    /* ========= DB/데이터 ========= */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("[409] DB Constraint: {}", ex.getMostSpecificCause().getMessage());
        return ResponseEntity
                .status(ApiErrorCode.DB_CONSTRAINT_VIOLATION.status())
                .body(ApiResponse.error(ApiErrorCode.DB_CONSTRAINT_VIOLATION.code(),
                        defaultMessageOr(ApiErrorCode.DB_CONSTRAINT_VIOLATION, "Constraint violation")));
    }

    /* ========= 외부 연동 ========= */
    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<ApiResponse<Void>> handleRestClient(RestClientResponseException ex) {
        log.warn("[502] External API: status={}, body={}", ex.getRawStatusCode(), ex.getResponseBodyAsString());
        return ResponseEntity
                .status(ApiErrorCode.EXTERNAL_API_ERROR.status())
                .body(ApiResponse.error(ApiErrorCode.EXTERNAL_API_ERROR.code(),
                        defaultMessageOr(ApiErrorCode.EXTERNAL_API_ERROR, "External API error")));
    }

    @ExceptionHandler({ResourceAccessException.class, SocketTimeoutException.class})
    public ResponseEntity<ApiResponse<Void>> handleExternalTimeout(Exception ex) {
        log.warn("[503/504] External timeout: {}", ex.getMessage());
        return ResponseEntity
                .status(ApiErrorCode.SERVICE_UNAVAILABLE.status())
                .body(ApiResponse.error(ApiErrorCode.SERVICE_UNAVAILABLE.code(),
                        defaultMessageOr(ApiErrorCode.SERVICE_UNAVAILABLE, "Service unavailable")));
    }

    /* ========= 마지막 안전망 ========= */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAll(Exception ex) {
        log.error("[500] Unexpected", ex);
        return ResponseEntity
                .status(ApiErrorCode.INTERNAL_ERROR.status())
                .body(ApiResponse.error(ApiErrorCode.INTERNAL_ERROR.code(),
                        defaultMessageOr(ApiErrorCode.INTERNAL_ERROR, "Unexpected error")));
    }

    /* ========= 유틸 ========= */
    private String defaultMessageOr(ApiErrorCode code, String fallback) {
        return (code.defaultMessage() != null && !code.defaultMessage().isBlank()) ? code.defaultMessage() : fallback;
    }

}
