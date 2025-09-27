package com.musinsa.freepoint.adapters.in.web.dto;

public record UseResponse(
        Long usageKey,
        String orderNo,
        long amount
) {}