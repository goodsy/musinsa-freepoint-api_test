
package com.musinsa.freepoint.adapters.in.web;

import com.musinsa.freepoint.application.service.UseCancelUseCase;
import com.musinsa.freepoint.application.service.UsePointUseCase;
import com.musinsa.freepoint.application.port.in.AccrualCommandPort.UseCommand;
import com.musinsa.freepoint.application.port.in.AccrualCommandPort.CancelUseCommand;
import com.musinsa.freepoint.domain.usage.PointUsage;
import org.springframework.web.bind.annotation.*;

record UseRequest(String userId, String orderNo, long amount) {}
record UseResponse(Long usageId, String userId, String orderNo, long amount) {}
record CancelUseRequest(long amount, String reason) {}

@RestController
@RequestMapping("/api/v1/points/usages")
public class UsageController {
    private final UsePointUseCase useCase; private final UseCancelUseCase cancelUseCase;
    public UsageController(UsePointUseCase useCase, UseCancelUseCase cancelUseCase) { this.useCase = useCase; this.cancelUseCase = cancelUseCase; }

    @PostMapping
    public UseResponse use(@RequestBody UseRequest req) {
        PointUsage u = useCase.use(new UseCommand(req.userId(), req.orderNo(), req.amount()));
        return new UseResponse(u.getId(), u.getUserId(), u.getOrderNo(), u.getAmount());
    }

    @PostMapping("/{usageId}/cancel")
    public UseResponse cancel(@PathVariable Long usageId, @RequestBody CancelUseRequest req) {
        PointUsage u = cancelUseCase.cancel(new CancelUseCommand(usageId, req.amount(), req.reason()));
        return new UseResponse(u.getId(), u.getUserId(), u.getOrderNo(), u.getAmount());
    }
}
