package com.musinsa.freepoint.application.port.in;

public interface IdempotencyPort {
    boolean existsByKey(String idempotencyKey);
    void saveKey(String idempotencyKey);
}
