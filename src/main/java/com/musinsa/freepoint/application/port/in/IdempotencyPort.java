package com.musinsa.freepoint.application.port.in;

import java.util.Optional;

public interface IdempotencyPort {

    Optional<CachedResponse> acquireOrCached(String idemKey, String method, String uri,
                                             String headers, String requestBody);
    void complete(String idemKey, int status, String responseBody);
    record CachedResponse(int statusCode, String body) {}

}
