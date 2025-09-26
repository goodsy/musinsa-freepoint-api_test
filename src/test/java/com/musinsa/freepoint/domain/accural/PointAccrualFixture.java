package com.musinsa.freepoint.domain.accural;

import com.musinsa.freepoint.adapters.in.web.dto.AccrualRequest;
import com.musinsa.freepoint.application.port.in.AccrualCommandPort;

public final class PointAccrualFixture {

    private PointAccrualFixture() {}


    public static AccrualCommandPort.CreateAccrual createPointAccrualCommand(
            String userId,
            long amount,
            int expiryDays,
            boolean manual,
            String sourceType,
            String sourceId) {

        AccrualRequest req = new AccrualRequest(userId, amount, expiryDays, manual, sourceType, sourceId);
        return new AccrualCommandPort.CreateAccrual(req);
    }

    // 편한 기본 생성 오버로드
    public static AccrualCommandPort.CreateAccrual pointAccrualCommand(String userId, long amount) {
        return createPointAccrualCommand(userId, amount, 7, false, "ORDER", "order-123");
    }


    public static AccrualRequest createPointAccrualReqeust(
            String userId,
            long amount,
            int expiryDays,
            boolean manual,
            String sourceType,
            String sourceId) {

        AccrualRequest req = new AccrualRequest(userId, amount, expiryDays, manual, sourceType, sourceId);
        return req;
    }

    // 편한 기본 생성 오버로드
    public static AccrualRequest caretePointAccrualRequest(String userId, long amount) {
        return createPointAccrualReqeust(userId, amount, 7, false, "ORDER", "order-123");
    }
}
