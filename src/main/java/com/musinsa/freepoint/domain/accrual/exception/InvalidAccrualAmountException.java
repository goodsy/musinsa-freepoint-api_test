package com.musinsa.freepoint.domain.accrual.exception;

import com.musinsa.freepoint.domain.DomainException;

public class InvalidAccrualAmountException extends DomainException {
    public InvalidAccrualAmountException(long amount, long min, long max) {
        super("FP-ACCRUAL-001", "적립 금액이 허용 범위를 벗어났습니다: amount=" + amount + ", min=" + min + ", max=" + max);
    }
}
