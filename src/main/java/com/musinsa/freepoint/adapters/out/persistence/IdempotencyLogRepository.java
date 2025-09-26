package com.musinsa.freepoint.adapters.out.persistence;

import com.musinsa.freepoint.common.logging.ApiLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyLogRepository extends JpaRepository<ApiLog, Long> {
    boolean existsByIdempotencyKey(String idempotencyKey);
}