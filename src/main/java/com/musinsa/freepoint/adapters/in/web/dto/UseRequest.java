package com.musinsa.freepoint.adapters.in.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record UseRequest(
        @NotBlank String userId,
        @NotBlank String orderNo,
        @Min(1) long amount
) {}