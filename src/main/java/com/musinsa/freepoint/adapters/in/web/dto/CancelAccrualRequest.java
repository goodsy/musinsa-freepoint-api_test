package com.musinsa.freepoint.adapters.in.web.dto;
import jakarta.validation.constraints.NotBlank;

public record CancelAccrualRequest(
        @NotBlank String userId,
        @NotBlank String pointKey
) {}
