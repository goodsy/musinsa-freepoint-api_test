package com.musinsa.freepoint.adapters.out.persistence;

import com.musinsa.freepoint.domain.log.ApiLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyRecordRepository extends JpaRepository<ApiLog, String> {
    Optional<ApiLog> findByIdempotencyKey(String key);
}
