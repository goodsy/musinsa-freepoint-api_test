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

    @Override
    public boolean existsByKey(String idempotencyKey) {
        return false;
    }

    @Override
    public void saveKey(String idempotencyKey) {

    }

    @Override
    public Optional<ApiLogSnapshot> findByKey(String idempotencyKey) {
        return Optional.empty();
    }

    @Override
    public boolean tryInsertPlaceholder(String idempotencyKey, String method, String path, String requestHeaders) {
        return false;
    }

    @Override
    public void updateCompletion(String idempotencyKey, int statusCode, String responseBody, String finalRequestHeaders, String finalRequestBody) {

    }


    /*private final IdempotencyPort idempotencyPort;

    *//** 중복 요청에 대해 즉시 반환할 캐시 응답 *//*
    public record CachedResponse(int statusCode, String body) {}

    @Transactional
    public boolean checkAndSaveKey(String idempotencyKey) {
        if (idempotencyPort.existsByKey(idempotencyKey)) {
            return false;
        }
        idempotencyPort.saveKey(idempotencyKey);
        return true;
    }

    *//**
     * 선점(placeholder) 시도.
     * - 성공(true) → 최초 진입, 컨트롤러 로직 계속 수행
     * - 실패(false) → 이미 키가 있음: 완료면 캐시 응답 반환, 아니면 409
     *//*
    @Transactional
    public Optional<CachedResponse> acquireOrCached(String key, String method, String path, String requestHeaders) {
        if (key == null || key.isBlank()) return Optional.empty();

        if (idempotencyPort.tryInsertPlaceholder(key, method, path, requestHeaders)) {
            // 최초 요청 진입 허용
            return Optional.empty();
        }

        // 중복: 완료 여부 확인
        var snap = idempotencyPort.findByKey(key).orElse(null);
        if (snap == null) {
            // 이례적 상황: 중복인데 조회가 안 되면 재시도 유도
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Idempotency in progress");
        }

        if (snap.statusCode() != null && !snap.statusCode().isBlank()) {
            int code = safeParse(snap.statusCode(), 200);
            return Optional.of(new CachedResponse(code, snap.responseBody() == null ? "" : snap.responseBody()));
        }

        // 아직 처리중
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Idempotency in progress");
    }

    *//** 완료 시점에 최종 응답/요청 본문/헤더를 기록 *//*
    @Transactional
    public void complete(String key, int statusCode, String responseBody,
                         String finalRequestHeaders, String finalRequestBody) {
        if (key == null || key.isBlank()) return;
        idempotencyPort.updateCompletion(key, statusCode, responseBody, finalRequestHeaders, finalRequestBody);
    }

    private static int safeParse(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }*/
}
