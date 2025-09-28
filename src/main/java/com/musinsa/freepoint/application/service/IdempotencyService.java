package com.musinsa.freepoint.application.service;

import com.musinsa.freepoint.application.port.in.IdempotencyPort;
import com.musinsa.freepoint.application.port.out.ApiLogPort;
import com.musinsa.freepoint.domain.KeyGenerator;
import com.musinsa.freepoint.domain.log.ApiLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IdempotencyService implements IdempotencyPort{

    private final ApiLogPort apiLogPort;

    @Override
    @Transactional
    public Optional<CachedResponse> acquireOrCached(String idemKey, String method, String uri,
                                                    String headers, String requestBody) {
        if (idemKey == null || idemKey.isBlank()) return Optional.empty();

        // 기존 응답이 있으면 재전송 (캐시 응답)
        var existing = apiLogPort.findByIdempotencyKey(idemKey);

        if (existing.isPresent()) {
            var e = existing.get();
            if (e.getStatusCode() != null) {
                // 이미 완료된 요청 → 동일 응답 재전달
                int sc = Integer.parseInt(e.getStatusCode());
                return Optional.of(new CachedResponse(sc, e.getResponseBody()));
            }
            // 아직 처리중(PENDING) → 후속 처리 (대기, 409 등 선택 가능)
        }

        // 최초 요청
        String logId = KeyGenerator.generateApiLogId();
        apiLogPort.insertRequest(ApiLog.builder()
                .logId(logId)
                .apiMethod(method)
                .apiUri(uri)
                .idempotencyKey(idemKey)
                .requestHeaders(headers)
                .requestBody(requestBody)
                .build());
        return Optional.empty();
    }

    @Override
    @Transactional
    public void complete(String idemKey, int status, String responseBody) {
        var existing = apiLogPort.findByIdempotencyKey(idemKey);
        existing.ifPresent(e -> apiLogPort.updateResponse(e.getLogId(), status, responseBody));
    }

}
