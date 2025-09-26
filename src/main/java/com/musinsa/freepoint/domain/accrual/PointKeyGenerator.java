package com.musinsa.freepoint.common.util;

import java.util.UUID;

public class PointKeyGenerator {
    public static String generatePointKey(String userId) {
        return userId + "-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
