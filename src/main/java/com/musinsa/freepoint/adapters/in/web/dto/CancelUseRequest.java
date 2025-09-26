package com.musinsa.freepoint.adapters.in.web.dto;

import jakarta.validation.constraints.Min;

public record CancelUseRequest(
        @Min(1) long amount,
        String reason
) {}