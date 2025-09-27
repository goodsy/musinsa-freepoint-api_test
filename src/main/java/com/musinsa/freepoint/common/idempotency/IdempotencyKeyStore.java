package com.musinsa.freepoint.common.idempotency;

import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class IdempotencyKeyStore {
    private final ConcurrentHashMap<String, Instant> keyStore = new ConcurrentHashMap<>();

    public boolean exists(String key) {
        return keyStore.containsKey(key);
    }

    public void save(String key) {
        keyStore.put(key, Instant.now());
    }
}
