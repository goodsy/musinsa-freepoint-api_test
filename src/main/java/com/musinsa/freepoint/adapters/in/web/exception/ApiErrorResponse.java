package com.musinsa.freepoint.adapters.in.web.exception;

import java.time.OffsetDateTime;

public record ApiErrorResponse(
        String code,
        int httpStatus,
        String message,
        String path,
        OffsetDateTime timestamp
) {
    public static ApiErrorResponse of(ApiErrorCode ec, String message, String path) {
        return new ApiErrorResponse(
                ec.code(),
                ec.status().value(),
                message,
                path,
                OffsetDateTime.now()
        );
    }
}
