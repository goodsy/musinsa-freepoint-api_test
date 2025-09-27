package com.musinsa.freepoint.adapters.in.web.dto;

public record CancelUseResponse(
        Long usageId,
        String orderNo,
        long usedAmount
) {}