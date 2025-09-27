package com.musinsa.freepoint.adapters.in.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CancelUseRequest(
        @NotBlank long usageId,
        @Min(1) long canceledAmount
) {}