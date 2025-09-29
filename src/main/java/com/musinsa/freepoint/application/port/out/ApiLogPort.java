package com.musinsa.freepoint.application.port.out;


import com.musinsa.freepoint.domain.log.ApiLog;

import java.util.Optional;

public interface ApiLogPort {
    public void insertRequest(ApiLog apiLog) ;
    void updateResponse(String logId, int httpStatus, String body); // 완료/에러 UPDATE
    Optional<ApiLog> findByIdempotencyKey(String idemKey);
    public Optional<ApiLog> findByLogId(String logId);
}
