
package com.musinsa.freepoint.adapters.in.web;

import com.musinsa.freepoint.adapters.in.web.dto.AccrualRequest;
import com.musinsa.freepoint.adapters.in.web.dto.AccrualResponse;
import com.musinsa.freepoint.application.port.in.AccrualCommandPort.CreateAccrual;
import com.musinsa.freepoint.application.service.AccrualUseCase;
import com.musinsa.freepoint.common.idempotency.Idempotent;
import com.musinsa.freepoint.domain.accrual.PointAccrual;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/points/accruals")
public class AccrualController {
    private final AccrualUseCase useCase;

    public AccrualController(AccrualUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @Idempotent
    public AccrualResponse accrue(@RequestBody AccrualRequest request) {

        PointAccrual accrual = useCase.accrue(new CreateAccrual(request));

        return new AccrualResponse(
                accrual.getPointKey(),
                accrual.getUserId(),
                accrual.getAmount(),
                accrual.getRemainAmount(),
                accrual.isManual()
        );
    }
}
