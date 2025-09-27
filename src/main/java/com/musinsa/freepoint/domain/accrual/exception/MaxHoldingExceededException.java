package com.musinsa.freepoint.domain.accrual.exception;


import com.musinsa.freepoint.domain.DomainException;

public class MaxHoldingExceededException extends DomainException {
    public MaxHoldingExceededException(String userId, long current, long limit) {
        super("FP-ACCRUAL-002", "개인 보유 한도를 초과합니다: userId=" + userId + ", current=" + current + ", limit=" + limit);
    }
}
