package com.musinsa.freepoint.adapters.in.web.dto;

public record UseResponse(
        Long usageId,
        String userId,
        String orderNo,
        long amount
) {}