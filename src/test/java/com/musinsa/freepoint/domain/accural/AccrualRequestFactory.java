package com.musinsa.freepoint.domain.accural;
import com.musinsa.freepoint.adapters.in.web.dto.AccrualRequest;

public final class AccrualRequestFactory {

    private AccrualRequestFactory() {}

    public static AccrualRequest create(String userId, long amount, int expiryDays, boolean manual, String sourceType, String sourceId) {
        return new AccrualRequest(userId, amount, expiryDays, manual, sourceType, sourceId);
    }

    public static AccrualRequest create(String userId, long amount) {
        return create(userId, amount, 7, false, "ORDER", "order-123");
    }

    public static AccrualRequest withExpiry(String userId, long amount, int expiryDays) {
        return create(userId, amount, expiryDays, false, "ORDER", "order-123");
    }

    public static AccrualRequest manual(String userId, long amount, boolean manual) {
        return create(userId, amount, 7, manual, "ORDER", "order-123");
    }
}
