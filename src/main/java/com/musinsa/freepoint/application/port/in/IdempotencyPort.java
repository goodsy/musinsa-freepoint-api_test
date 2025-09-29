package com.musinsa.freepoint.application.port.in;

import java.util.Optional;

public interface IdempotencyPort {

    Optional<CachedResponse> preCheck(String idemKey);
    void saveOrUpdate(String idemKey, int status, String responseBody);
    record CachedResponse(int statusCode, String headers, String body) {}

}
