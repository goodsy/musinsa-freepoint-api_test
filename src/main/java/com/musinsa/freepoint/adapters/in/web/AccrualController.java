
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
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/points/accruals")
public class AccrualController {
    private final AccrualUseCase useCase;

    public AccrualController(AccrualUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @IdempotencyKey //@RequestAttribute("MUSINSA_ID") String musinsaId,
    public ResponseEntity<ApiResponse<AccrualResponse>> accrue(@Valid @RequestBody AccrualRequest request) {

        PointAccrual accrual = useCase.accrue(new CreateAccrual(request));

        AccrualResponse response = new AccrualResponse(
                accrual.getPointKey(),
                accrual.getUserId(),
                accrual.getAmount(),
                accrual.getExpireAt()
        );

        return ResponseEntity.ok(ApiResponse.ok(response));

    }


    @PostMapping("/cancel")
    @IdempotencyKey
    public ResponseEntity<ApiResponse<CancelAccrualResponse>> cancelAccrual(@Valid @RequestBody CancelAccrualRequest request) {
        PointAccrual accrual = useCase.cancelAccrual(new CancelAccrual(request));

        CancelAccrualResponse response = new CancelAccrualResponse(
                accrual.getPointKey(),
                accrual.getUserId(),
                accrual.getAmount()
        );

        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
