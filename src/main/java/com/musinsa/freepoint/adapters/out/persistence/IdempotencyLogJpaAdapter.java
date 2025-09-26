package com.musinsa.freepoint.adapters.out.persistence;

import com.musinsa.freepoint.application.port.IdempotencyPort;
import com.musinsa.freepoint.common.logging.ApiLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IdempotencyLogJpaAdapter implements IdempotencyPort {
    private final IdempotencyLogRepository repository;

    @Override
    public boolean existsByKey(String idempotencyKey) {
        return repository.existsByIdempotencyKey(idempotencyKey);
    }

    @Override
    public void saveKey(String idempotencyKey) {
        repository.save(ApiLog.of(idempotencyKey));
    }
}