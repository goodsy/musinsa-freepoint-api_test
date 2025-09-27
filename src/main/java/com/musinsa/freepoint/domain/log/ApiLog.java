package com.musinsa.freepoint.domain.log;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "api_log")
public class ApiLog {
    @Id
    @Column(name="log_id", length=50)
    private String logId;

    @Column(name="api_method", length=10)
    private String apiMethod;

    @Column(name="api_uri", length=255)
    private String apiUri;

    @Column(name="idempotency_key", length=128, unique = true)
    private String idempotencyKey;

    @Column(name="request_headers", length=1000)
    private String requestHeaders;

    @Lob
    @Column(name="request_body")
    private String requestBody;

    @Lob
    @Column(name="response_body")
    private String responseBody;

    @Column(name="status_code", length=5)
    private String statusCode;

    @Column(name="created_at", insertable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

/*
    public ApiLog(String apiMethod, String apiUri, String requestBody, String requestHeaders) {
        generateLogId();

        this.apiMethod = apiMethod;
        this.apiUri = apiUri;
        this.requestBody = requestBody;
        this.requestHeaders = requestHeaders;
    }
*/


    public static String generateLogId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 15);
        return timestamp + uuid;
    }


    public static ApiLog of(String key) {
        ApiLog log = new ApiLog();

        log.generateLogId();
        log.idempotencyKey = key;

        return log;
    }
}