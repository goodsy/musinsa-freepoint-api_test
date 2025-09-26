package com.musinsa.freepoint.domain.accrual;

import java.util.UUID;

public class PointKeyGenerator {
    public static String generatePointKey(String userId) {
        return userId + "-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
