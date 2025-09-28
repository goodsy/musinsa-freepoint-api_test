package com.musinsa.freepoint.adapters.in.web.dto;

public record UseResponse(
        String usageKey,
        String orderNo,
        long amount
) {}