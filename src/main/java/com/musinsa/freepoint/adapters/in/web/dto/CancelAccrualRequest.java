package com.musinsa.freepoint.adapters.in.web.dto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CancelAccrualRequest(
        @NotBlank String userId,
        @NotBlank String pointKey,
        @Min(1) Long amount,
        String reason

) {}
