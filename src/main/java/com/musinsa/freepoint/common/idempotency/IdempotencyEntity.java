
package com.musinsa.freepoint.common.idempotency;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "idempotency_registry")
public class IdempotencyEntity {
    @Id @Column(name = "idem_key", length = 128)
    private String key;
    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;
    private String method;
    private String path;
    @Lob private String responseBody;
    private Integer status;
    private Instant createdAt = Instant.now();
    public static IdempotencyEntity of(String key, String hash, String method, String path, String res, int status) {
        IdempotencyEntity e = new IdempotencyEntity();
        e.key = key; e.requestHash = hash; e.method = method; e.path = path; e.responseBody = res; e.status = status;
        return e;
    }
    public String getKey() { return key; }
    public String getRequestHash() { return requestHash; }
    public String getResponseBody() { return responseBody; }
    public Integer getStatus() { return status; }
}
