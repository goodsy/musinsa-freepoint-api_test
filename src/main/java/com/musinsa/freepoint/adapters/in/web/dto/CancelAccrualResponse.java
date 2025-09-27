package com.musinsa.freepoint.adapters.in.web.dto;


public record CancelAccrualResponse(
        String pointKey,
        String userId,
        long amount
) {}