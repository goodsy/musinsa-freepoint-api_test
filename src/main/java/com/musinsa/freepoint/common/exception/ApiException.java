
package com.musinsa.freepoint.common.exception;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * 가장 많이 쓰는 생성자/팩토리만 노출한 간결한 예외.
 * - status, errorCode, message, props(추가 컨텍스트)만 유지
 * - DX를 위해 정적 팩토리 제공
 */
public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final ErrorCode errorCode;
    private final Map<String, Object> props;

    public ApiException(HttpStatus status, ErrorCode errorCode, String message) {
        this(status, errorCode, message, null, null);
    }

    public ApiException(HttpStatus status, ErrorCode errorCode, String message, Throwable cause) {
        this(status, errorCode, message, cause, null);
    }

    public ApiException(HttpStatus status, ErrorCode errorCode, String message, Map<String, Object> props) {
        this(status, errorCode, message, null, props);
    }

    public ApiException(HttpStatus status, ErrorCode errorCode, String message, Throwable cause, Map<String, Object> props) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
        this.props = props;
    }

    public HttpStatus getStatus() { return status; }
    public ErrorCode getErrorCode() { return errorCode; }
    public Map<String, Object> getProps() { return props; }

    public static ApiException badRequest(ErrorCode code, String msg) {
        return new ApiException(HttpStatus.BAD_REQUEST, code, msg);
    }
    public static ApiException unauthorized(String msg) {
        return new ApiException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, msg);
    }
    public static ApiException forbidden(String msg) {
        return new ApiException(HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN, msg);
    }
    public static ApiException notFound(ErrorCode code, String msg) {
        return new ApiException(HttpStatus.NOT_FOUND, code, msg);
    }
    public static ApiException conflict(ErrorCode code, String msg) {
        return new ApiException(HttpStatus.CONFLICT, code, msg);
    }
    public static ApiException internal(String msg, Throwable cause) {
        return new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR, msg, cause);
    }
}