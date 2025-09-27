package com.musinsa.freepoint.adapters.out.persistence;

import com.musinsa.freepoint.domain.log.ApiLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyLogRepository extends JpaRepository<ApiLog, String> {
    boolean existsByIdempotencyKey(String idempotencyKey);
    Optional<ApiLog> findByIdempotencyKey(String idempotencyKey);
}