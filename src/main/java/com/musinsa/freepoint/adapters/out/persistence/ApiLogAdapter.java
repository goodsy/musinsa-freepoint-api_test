package com.musinsa.freepoint.adapters.out.persistence;

import com.musinsa.freepoint.application.port.out.ApiLogPort;
import com.musinsa.freepoint.domain.log.ApiLog;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ApiLogAdapter implements ApiLogPort {
    private final ApiLogRepository apiLogRepository;

    public ApiLogAdapter(ApiLogRepository apiLogRepository) {
        this.apiLogRepository = apiLogRepository;
    }


   @Override
    public void insertRequest(ApiLog apiLog) {
       apiLogRepository.save(apiLog);
   }

    @Override
    public void updateResponse(String logId, int httpStatus, String body) {
        // 실제 구현
        apiLogRepository.findById(logId).ifPresent(ent -> {
            ent.setStatusCode(Integer.toString(httpStatus));
            ent.setResponseBody(body);
            apiLogRepository.save(ent);
        });
    }

    @Override
    public Optional<ApiLog> findByIdempotencyKey(String idemKey) {
        // 실제 구현
        return apiLogRepository.findByIdempotencyKey(idemKey).map(this::toDomain);
    }

    @Override
    public Optional<ApiLog> findByLogId(String logId) {
        return apiLogRepository.findById(logId).map(this::toDomain);
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