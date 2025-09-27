package com.musinsa.freepoint.adapters.in.web.dto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AccrualRequest(
        @NotBlank String userId,
        @Min(1) long amount,
        @Min(1) int expiryDays,
        Boolean manual,
        String sourceType,
        String sourceId
) {}
