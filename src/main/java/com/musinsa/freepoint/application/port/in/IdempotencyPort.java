package com.musinsa.freepoint.application.port;

public interface IdempotencyPort {
    boolean existsByKey(String idempotencyKey);
    void saveKey(String idempotencyKey);
}
