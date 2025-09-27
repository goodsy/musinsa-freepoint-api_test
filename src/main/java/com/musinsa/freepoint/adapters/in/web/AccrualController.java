
package com.musinsa.freepoint.adapters.in.web;

import com.musinsa.freepoint.adapters.in.web.dto.AccrualRequest;
import com.musinsa.freepoint.adapters.in.web.dto.AccrualResponse;
import com.musinsa.freepoint.adapters.in.web.dto.CancelAccrualRequest;
import com.musinsa.freepoint.adapters.in.web.dto.CancelAccrualResponse;
import com.musinsa.freepoint.application.port.in.AccrualCommandPort.CreateAccrual;
import com.musinsa.freepoint.application.port.in.AccrualCommandPort.CancelAccrual;
import com.musinsa.freepoint.application.service.AccrualUseCase;
import com.musinsa.freepoint.common.idempotency.IdempotencyKey;
import com.musinsa.freepoint.domain.accrual.PointAccrual;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/points/accruals")
public class AccrualController {
    private final AccrualUseCase useCase;

    public AccrualController(AccrualUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @IdempotencyKey
    public AccrualResponse accrue(@RequestBody AccrualRequest request) {

        PointAccrual accrual = useCase.accrue(new CreateAccrual(request));

        return new AccrualResponse(
                accrual.getPointKey(),
                accrual.getUserId(),
                accrual.getAmount(),
                accrual.getExpireAt()
        );
    }


    @PostMapping("/cancel/{pointKey}")
    @IdempotencyKey
    public CancelAccrualResponse cancelAccrual(@RequestBody CancelAccrualRequest request) {
        PointAccrual accrual = useCase.cancelAccrual(new CancelAccrual(request));

        return new CancelAccrualResponse(
                accrual.getPointKey(),
                accrual.getUserId(),
                accrual.getAmount()
        );
    }
}
