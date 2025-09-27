package com.musinsa.freepoint.application.port.in;

import java.util.Optional;

public interface IdempotencyPort {

    // 멱등키 존재하면 캐시된 응답 반환, 없으면 선점(log_id 생성 후 진행)
    Optional<CachedResponse> acquireOrCached(String idemKey, String method, String uri,
                                             String headers, String requestBody);

    void complete(String idemKey, int status, String responseBody);

    record CachedResponse(int statusCode, String body) {}

    /** 애플리케이션 계층에서 쓰는 최소 정보 스냅샷 */
    record ApiLogSnapshot(String idempotencyKey, String statusCode, String responseBody) {}
}
