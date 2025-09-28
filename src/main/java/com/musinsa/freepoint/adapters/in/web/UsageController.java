
package com.musinsa.freepoint.adapters.in.web;

import com.musinsa.freepoint.adapters.in.web.dto.*;
import com.musinsa.freepoint.application.port.in.UsageCommandPort.*;
import com.musinsa.freepoint.application.service.UseagePointUseCase;
import com.musinsa.freepoint.domain.usage.PointUsage;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/points/usages")
public class UsageController {
    private final UseagePointUseCase useCase;

    public UsageController(UseagePointUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UseResponse>> use(@Valid @RequestBody UseRequest request) {
        PointUsage usage = useCase.usage(new UsagePoint(request));

        UseResponse response = new UseResponse(
                usage.getUsageKey(), usage.getOrderNo(), usage.getAmount()
        );

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/cancel")
    public CancelUseResponse cancel(@Valid @RequestBody CancelUseRequest request) {
        PointUsage usage = useCase.cancel(new CancelUsagePoint(request));
        return new CancelUseResponse(usage.getUsageKey(), usage.getOrderNo(), usage.getAmount());

    }
}
