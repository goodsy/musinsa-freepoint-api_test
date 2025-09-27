
package com.musinsa.freepoint.adapters.in.web;

import com.musinsa.freepoint.adapters.in.web.dto.CancelUseRequest;
import com.musinsa.freepoint.adapters.in.web.dto.CancelUseResponse;
import com.musinsa.freepoint.adapters.in.web.dto.UseRequest;
import com.musinsa.freepoint.adapters.in.web.dto.UseResponse;
import com.musinsa.freepoint.application.port.in.UsageCommandPort.*;
import com.musinsa.freepoint.application.service.UseagePointUseCase;
import com.musinsa.freepoint.domain.usage.PointUsage;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/points/usages")
public class UsageController {
    private final UseagePointUseCase useCase;

    public UsageController(UseagePointUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public UseResponse use(@RequestBody UseRequest request) {
        PointUsage usage = useCase.usage(new UsagePoint(request));
        return new UseResponse(usage.getId(), usage.getOrderNo(), usage.getAmount());
    }

    @PostMapping("/cancel/{usageId}")
    public CancelUseResponse cancel(@PathVariable Long usageId, @RequestBody CancelUseRequest request) {
        PointUsage usage = useCase.cancel(new CancelUsagePoint(request));
        return new CancelUseResponse(usage.getId(), usage.getOrderNo(), usage.getAmount());

    }
}
