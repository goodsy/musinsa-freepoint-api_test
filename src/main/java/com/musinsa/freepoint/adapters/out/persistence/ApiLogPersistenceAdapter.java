package com.musinsa.freepoint.adapters.out.persistence;

import com.musinsa.freepoint.application.port.out.ApiLogPort;
import com.musinsa.freepoint.domain.log.ApiLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ApiLogPersistenceAdapter implements ApiLogPort {

    private final ApiLogJpaRepository repo;

    @Override
    public void insertRequest(ApiLog e) {
        var ent = ApiLog.builder()
                .logId(e.getLogId())
                .apiMethod(e.getApiMethod())
                .apiUri(e.getApiUri())
                .idempotencyKey(e.getIdempotencyKey())
                .requestHeaders(e.getRequestHeaders())
                .requestBody(e.getRequestBody())
                .build();
        repo.save(ent);
    }

    @Override
    public void updateResponse(String logId, int httpStatus, String body) {
        repo.findById(logId).ifPresent(ent -> {
            ent.setStatusCode(Integer.toString(httpStatus));
            ent.setResponseBody(body);
            repo.save(ent);
        });
    }

    @Override
    public Optional<ApiLog> findByIdempotencyKey(String idemKey) {
        return repo.findByIdempotencyKey(idemKey).map(this::toDomain);
    }

    @Override
    public Optional<ApiLog> findByLogId(String logId) {
        return repo.findById(logId).map(this::toDomain);
    }

    private ApiLog toDomain(ApiLog e) {
        return ApiLog.builder()
                .logId(e.getLogId())
                .apiMethod(e.getApiMethod())
                .apiUri(e.getApiUri())
                .idempotencyKey(e.getIdempotencyKey())
                .requestHeaders(e.getRequestHeaders())
                .requestBody(e.getRequestBody())
                .responseBody(e.getResponseBody())
                .statusCode(e.getStatusCode())
                .build();
    }
}
