package com.musinsa.freepoint.adapters.in.web.dto;


import java.time.LocalDateTime;

public record AccrualResponse(
        String pointKey,
        String userId,
        long amount,
        LocalDateTime expiryAt
) {}