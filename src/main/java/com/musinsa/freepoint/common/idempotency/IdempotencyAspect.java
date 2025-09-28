package com.musinsa.freepoint.common.idempotency;

import com.musinsa.freepoint.adapters.in.web.ApiHeaderConstants;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Aspect
@Component
@RequiredArgsConstructor
public class IdempotencyAspect {
    private final HttpServletRequest request;
    private final IdempotencyKeyStore keyStore;

    @Around("@annotation(com.musinsa.freepoint.common.idempotency.IdempotencyKey)")
    public Object checkIdempotency(ProceedingJoinPoint joinPoint) throws Throwable {
        String key = request.getHeader(ApiHeaderConstants.IDEMPOTENCY_KEY);

        if (key == null || key.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Idempotency-Key 헤더가 필요합니다.");
        }
        if (keyStore.exists(key)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 처리된 요청입니다.");
        }

        keyStore.save(key);
        return joinPoint.proceed();
    }
}