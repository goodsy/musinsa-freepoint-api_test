package com.musinsa.freepoint.adapters.in.web.dto;

public record CancelUseResponse(
        String usageKey,
        String orderNo,
        long amount
) {}