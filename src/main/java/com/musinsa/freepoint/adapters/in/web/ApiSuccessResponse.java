package com.musinsa.freepoint.adapters.in.web;

public record ApiSuccessResponse<T>(
        boolean success,
        T data,
        String code,
        String message
) {

    public static <T> ApiSuccessResponse<T> of(T data) {
        return new ApiSuccessResponse<>(true, data, ApiSuccessCode.CODE, ApiSuccessCode.MESSAGE);
    }

    public static <T> ApiSuccessResponse<T> of(T data, String message) {
        return new ApiSuccessResponse<>(true, data, ApiSuccessCode.CODE, message);
    }
}
