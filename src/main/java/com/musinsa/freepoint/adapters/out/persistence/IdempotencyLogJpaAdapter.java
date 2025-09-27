package com.musinsa.freepoint.adapters.out.persistence;

import com.musinsa.freepoint.application.port.in.IdempotencyPort;
import com.musinsa.freepoint.domain.log.ApiLog;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class IdempotencyLogJpaAdapter implements IdempotencyPort {
    private final IdempotencyLogRepository idempotencyLogRepo;

    @Override
    public Optional<CachedResponse> acquireOrCached(String idemKey, String method, String uri, String headers, String requestBody) {
        return Optional.empty();
    }

    @Override
    public void complete(String idemKey, int status, String responseBody) {

    }

    @Override
    public boolean existsByKey(String idempotencyKey) {
        return idempotencyLogRepo.existsByIdempotencyKey(idempotencyKey);
    }

    @Override
    public void saveKey(String idempotencyKey) {
        idempotencyLogRepo.save(ApiLog.of(idempotencyKey));
    }

    @Override
    public Optional<ApiLogSnapshot> findByKey(String idempotencyKey) {
        return idempotencyLogRepo.findByIdempotencyKey(idempotencyKey)
                .map(a -> new ApiLogSnapshot(a.getIdempotencyKey(), a.getStatusCode(), a.getResponseBody()));
    }

    @Override
    public boolean tryInsertPlaceholder(String idempotencyKey, String method, String uri, String requestHeaders) {
        try {

            ApiLog log = new ApiLog();
            log.generateLogId();
            log.setIdempotencyKey(idempotencyKey);
            log.setApiMethod(method);
            log.setApiUri(uri);
            log.setRequestHeaders(requestHeaders);

            // requestBody/responseBody/statusCode는 완료 시 업데이트
            idempotencyLogRepo.save(log);
            return true;
        } catch (DataIntegrityViolationException dup) {
            // UNIQUE(idempotency_key) 충돌 -> 이미 존재
            return false;
        }
    }

    @Override
    public void updateCompletion(String idempotencyKey, int statusCode, String responseBody,
                                 String finalRequestHeaders, String finalRequestBody) {
        ApiLog existing = idempotencyLogRepo.findByIdempotencyKey(idempotencyKey).orElse(null);
        if (existing == null) return;
        existing.setStatusCode(String.valueOf(statusCode));
        existing.setResponseBody(responseBody);
        if (finalRequestHeaders != null) existing.setRequestHeaders(finalRequestHeaders);
        if (finalRequestBody != null)   existing.setRequestBody(finalRequestBody);
        idempotencyLogRepo.save(existing);
    }
}