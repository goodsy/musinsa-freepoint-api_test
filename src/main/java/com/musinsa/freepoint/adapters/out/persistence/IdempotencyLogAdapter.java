package com.musinsa.freepoint.adapters.out.persistence;

import com.musinsa.freepoint.application.port.in.IdempotencyPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class IdempotencyLogAdapter implements IdempotencyPort {
    private final IdempotencyLogRepository idempotencyLogRepo;

    @Override
    public Optional<CachedResponse> acquireOrCached(String idemKey, String method, String uri, String headers, String requestBody) {
        return Optional.empty();
    }

    @Override
    public void complete(String idemKey, int status, String responseBody) {

    }


}