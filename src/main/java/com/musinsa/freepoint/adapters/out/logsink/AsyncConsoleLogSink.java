
package com.musinsa.freepoint.adapters.out.logsink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class AsyncConsoleLogSink implements ApiLogSink {
    private static final Logger log = LoggerFactory.getLogger(AsyncConsoleLogSink.class);
    private final ExecutorService exec = Executors.newFixedThreadPool(2);

    @Override
    public void enqueue(ApiLog logEntry) {
        exec.submit(() -> log.info("[API] rid={} method={} uri={} status={} took={}ms",
                logEntry.requestId(), logEntry.method(), logEntry.uri(),
                logEntry.status(), logEntry.tookMs()));
    }
}
