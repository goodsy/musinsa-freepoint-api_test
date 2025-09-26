package com.musinsa.freepoint.adapters.in.web.dto;


public record AccrualResponse(
        String pointKey,
        String userId,
        long amount,
        long remainAmount,
        boolean manual
) {}