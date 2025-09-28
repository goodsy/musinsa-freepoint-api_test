package com.musinsa.freepoint.adapters.in.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.musinsa.freepoint.adapters.in.web.exception.ApiErrorCode;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "success", "code", "message", "data", "errors" })
public record ApiResponse<T>(   boolean success,
                                String code,
                                String message,
                                @JsonInclude(JsonInclude.Include.NON_NULL) T data,
                                @JsonInclude(JsonInclude.Include.NON_EMPTY) List<FieldError> errors
) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, ApiSuccessCode.CODE, ApiSuccessCode.MESSAGE, data, null);
    }
    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, ApiSuccessCode.CODE, ApiSuccessCode.MESSAGE, null, null);
    }

    public static ApiResponse<Void> error(String code, String message) {
        return new ApiResponse<>(false, code, message, null, null);
    }

 /*   public static ApiResponse<Void> error(String code, String message, List<FieldError> errors) {
        List<FieldError> safe = (errors == null || errors.isEmpty()) ? null : List.copyOf(errors);
        return new ApiResponse<>(false, code, message, null, safe, OffsetDateTime.now());
    }
*/
    public record FieldError(String field, String reason, Object rejectedValue) {
        public static FieldError of(String field, String reason, Object rejectedValue) {
            return new FieldError(field, reason, rejectedValue);
        }
    }

    public static void filterSendError(HttpServletResponse response, int status, ApiErrorCode errorCode) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        Map<String, String> body = new HashMap<>();
        body.put("code", errorCode.code());
        body.put("message", errorCode.defaultMessage());
        new ObjectMapper().writeValue(response.getWriter(), body);
    }

}
