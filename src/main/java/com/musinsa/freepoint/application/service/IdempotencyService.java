package com.musinsa.freepoint.application.service;

import com.musinsa.freepoint.adapters.out.persistence.IdempotencyRecordRepository;
import com.musinsa.freepoint.application.port.in.IdempotencyPort;
import com.musinsa.freepoint.application.port.out.ApiLogPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IdempotencyService implements IdempotencyPort{

    //public record Cached(String statusCode, String responseHeaders, String responseBody) {}

    private final IdempotencyRecordRepository repository;
    private final ApiLogPort apiLogPort;

    @Transactional
    public Optional<IdempotencyPort.CachedResponse> preCheck(String idemKey) {
        if (idemKey == null || idemKey.isBlank()) return Optional.empty();

        // 기존 응답이 있으면 재전송 (캐시 응답)
        var existing = apiLogPort.findByIdempotencyKey(idemKey);

        if (existing.isPresent()) {
            var e = existing.get();
            if (e.getStatusCode() != null) {
                // 이미 완료된 요청 → 동일 응답 재전달
                int sc = Integer.parseInt(e.getStatusCode());
                return Optional.of(new IdempotencyPort.CachedResponse(sc, e.getRequestHeaders(), e.getResponseBody()));
            }
            // 아직 처리중(PENDING) → 후속 처리 (대기, 409 등 선택 가능)
        }
        return Optional.empty();

/*        return repository.findByIdempotencyKey(key)
                .filter(r -> r.getIdempotencyKey().equals(requestIdempKey))
                .filter(r -> r.getResponseBody() != null && r.getStatusCode() != null)
                .map(r -> new Cached(r.getStatusCode(), r.getIdempotencyKey(), r.getResponseBody()));*/
    }


    @Transactional
    public void saveOrUpdate(String idemKey, int status, String responseBody) {
        var existing = apiLogPort.findByIdempotencyKey(idemKey);
        existing.ifPresent(e -> apiLogPort.updateResponse(e.getLogId(), status, responseBody));

   /*     IdempotencyRecord record = repository.findByIdempotencyKey(key)
                .orElseGet(() -> IdempotencyRecord.builder()
                        .idempotencyKey(key)
                        .method(method)
                        .uri(uri)
                        .requestHash(requestHash)
                        .build());
        record.setStatusCode(statusCode);
        record.setResponseHeaders(responseHeaders);
        record.setResponseBody(responseBody);
        repository.save(record)*/;
    }
}
