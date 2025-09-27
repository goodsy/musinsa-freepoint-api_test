package com.musinsa.freepoint.domain.accrual.exception;

import com.musinsa.freepoint.domain.DomainException;

public class ExpiryPolicyViolationException extends DomainException {
    public ExpiryPolicyViolationException(int requestedExpiry, int minDays, int maxDays) {
        super("FP-ACCRUAL-003", "만료일 정책 위반: requested=" + requestedExpiry + ", minDays=" + minDays + ", maxDays=" + maxDays);
    }
}
