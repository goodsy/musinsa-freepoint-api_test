package com.musinsa.freepoint.application.service;

import com.musinsa.freepoint.application.port.in.IdempotencyPort;
import com.musinsa.freepoint.application.port.out.ApiLogPort;
import com.musinsa.freepoint.domain.log.ApiLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IdempotencyService implements IdempotencyPort{

    private final ApiLogPort apiLogPort;

    @Override
    @Transactional
    public Optional<CachedResponse> acquireOrCached(String idemKey, String method, String uri,
                                                    String headers, String requestBody) {
        if (idemKey == null || idemKey.isBlank()) return Optional.empty();

        // 1) 기존 응답이 있으면 “재전송” (캐시 응답)
        var existing = apiLogPort.findByIdempotencyKey(idemKey);
        if (existing.isPresent()) {
            var e = existing.get();
            if (e.getStatusCode() != null) {
                // 이미 완료된 요청 → 동일 응답 재전달
                int sc = Integer.parseInt(e.getStatusCode());
                return Optional.of(new CachedResponse(sc, e.getResponseBody()));
            }
            // 아직 처리중(PENDING) → 여기선 통과시켜도 되지만, 정책상 대기/409 선택 가능
        }

        // 2) 최초 요청 → api_log INSERT (status_code/response_body는 아직 비움)
        String logId = "LOG_" + UUID.randomUUID();
        apiLogPort.insertRequest(ApiLog.builder()
                .logId(logId)
                .apiMethod(method)
                .apiUri(uri)
                .idempotencyKey(idemKey)
                .requestHeaders(headers)
                .requestBody(requestBody)
                .build());
        return Optional.empty(); // 최초: 체인 계속 진행
    }

    @Override
    @Transactional
    public void complete(String idemKey, int status, String responseBody) {
        // idemKey로 logId 찾을 수도 있지만, 스키마 단순화를 위해 바로 UPDATE 해도 OK
        System.out.println("respBody11111 : "+responseBody);
        var existing = apiLogPort.findByIdempotencyKey(idemKey);
        existing.ifPresent(e -> apiLogPort.updateResponse(e.getLogId(), status, responseBody));
    }


}
