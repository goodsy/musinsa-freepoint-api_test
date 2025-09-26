
package com.musinsa.freepoint.application.port.in;

import com.musinsa.freepoint.adapters.in.web.dto.AccrualRequest;

public class AccrualCommandPort {
    //public record AccrualCommand(String pointKey, String userId, long amount, long expiryDays, boolean manual, String sourceType, String sourceId) {}
    public record CreateAccrual(AccrualRequest request) {}
    public record CancleAccrual(AccrualRequest request) {}
    public record UseCommand(String userId, String orderNo, long amount) {}
    public record CancelUseCommand(Long usageId, long amount, String reason) {}
}
