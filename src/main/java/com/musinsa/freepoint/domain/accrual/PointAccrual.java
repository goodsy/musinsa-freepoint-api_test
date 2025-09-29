
package com.musinsa.freepoint.domain.accrual;

import com.musinsa.freepoint.adapters.in.web.dto.AccrualRequest;
import com.musinsa.freepoint.adapters.in.web.exception.ApiErrorCode;
import com.musinsa.freepoint.domain.DomainException;
import com.musinsa.freepoint.domain.KeyGenerator;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static java.util.Objects.requireNonNull;
import static org.springframework.util.Assert.state;

@Entity
@Getter
@Setter
@Table(name = "point_accrual")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PointAccrual {
    @Id
    @Column(name="point_key", length = 50)
    private String pointKey;

    @Column(name="user_id",nullable = false)
    private String userId;

    @Column(name="amount",nullable = false)
    private long amount;

    @Column(name="remain_amount", nullable = false)
    private long remainAmount;

    @Column(name="manual", nullable = false)
    private boolean manual;

    @Column(name="source_type", nullable = false)
    private String sourceType;

    @Column(name="source_id", nullable = false)
    private String sourceId;

    @Column(name="expires_at", nullable = false, updatable = false)
    private LocalDateTime expireAt;

    @Column(name="status", nullable = false)
    private String status;

    @Column(name="created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name="updated_at", nullable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Version
    private int version;

    public static PointAccrual create(AccrualRequest request) {
        requireNonNull(request.userId(), "userId는 null일 수 없습니다");
        requireNonNull(request.sourceType(), "sourceType null일 수 없습니다");

        LocalDateTime now = LocalDateTime.now();
        return PointAccrual.builder()
                .pointKey(KeyGenerator.generatePointKey())
                .userId(request.userId())
                .amount(request.amount())
                .remainAmount(request.amount())
                .manual(request.manual())
                .sourceType(request.sourceType())
                .sourceId(request.sourceId())
                .expireAt(now.plusDays(request.expiryDays()))
                .status(AccrualStatus.ACTIVE.name())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static PointAccrual createReversal(String userId, long amount, String sourceId, int expiryDays) {
        return PointAccrual.builder()
                .pointKey(KeyGenerator.generatePointKey())
                .userId(userId)
                .amount(amount)
                .remainAmount(amount)
                .manual(false)
                .sourceType(AccrualSourceType.REVERSAL.name())
                .sourceId(sourceId)
                .expireAt(LocalDateTime.now().plusDays(expiryDays))
                .status(AccrualStatus.ACTIVE.name())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public boolean isActive() {
        return this.status == AccrualStatus.ACTIVE.name();
    }

    public boolean isExceedAmount(long amount) {
        return this.amount < amount ;
    }

    public boolean isExpired() {
        return (expireAt != null && LocalDateTime.now().isAfter(expireAt))
                || AccrualStatus.EXPIRED.name().equals(this.status);
    }


    public void cancel() {
        if (this.remainAmount != this.amount) {
            throw new DomainException(ApiErrorCode.USED_POINT_CANCELLATION_NOT_ALLOWED);
        }
        this.status = AccrualStatus.CANCELED.name();
        this.updatedAt = LocalDateTime.now();
    }

    public void use(long amount) {
        if (amount < 0 || amount > remainAmount) throw new DomainException(ApiErrorCode.INSUFFICIENT_BALANCE);
        this.remainAmount -= amount;
        this.updatedAt = LocalDateTime.now();
    }

    public void restore(long amount) {
        this.remainAmount += amount;
        this.updatedAt = LocalDateTime.now();
    }
}