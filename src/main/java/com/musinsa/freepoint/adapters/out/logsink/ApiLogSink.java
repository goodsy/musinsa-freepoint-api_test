
package com.musinsa.freepoint.adapters.out.logsink;

import java.util.Map;

public interface ApiLogSink {
    void enqueue(ApiLog log);

    record ApiLog(
        String requestId,
        String method,
        String uri,
        Map<String,String> headers,
        String requestBody,
        int status,
        String responseBody,
        long tookMs
    ) {}
}
