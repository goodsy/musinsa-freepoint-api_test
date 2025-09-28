package com.musinsa.freepoint.adapters.in.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CancelUseRequest(
        @NotBlank String usageKey,
        @Min(1) long amount
) {}