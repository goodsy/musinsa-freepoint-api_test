
package com.musinsa.freepoint.domain.accrual;
import com.musinsa.freepoint.adapters.in.web.dto.AccrualRequest;
import com.musinsa.freepoint.common.PointKeyGenerator;
import com.musinsa.freepoint.domain.model.Enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

import static java.util.Objects.requireNonNull;

@Entity
@Getter
@Setter
@Table(name = "point_accrual")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PointAccrual {
    @Id
    @Column(length = 100)
    private String pointKey;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private long amount;

    @Column(nullable = false)
    private long remainAmount;

    @Column(nullable = false)
    private boolean manual;

    @Column(nullable = false)
    private String sourceType;

    @Column(nullable = false)
    private String sourceId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime expireAt;

    @Column(nullable = false)
    private String status;

    private String idempotencyKey;
    private String createdBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private int version;

    public static PointAccrual create(String userId, long amount, boolean manual, String sourceType, String sourceId, LocalDateTime expiresAt) {
        return PointAccrual.builder()
                .pointKey(PointKeyGenerator.generatePointKey(userId))
                .userId(userId)
                .amount(amount)
                .remainAmount(amount)
                .manual(manual)
                .sourceType(sourceType)
                .sourceId(sourceId)
                .expireAt(LocalDateTime.now())
                .status(AccrualStatus.ACTIVE.name())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }


    public static PointAccrual create(AccrualRequest request) {
        //requireNonNull(request.userId(), "userId");
        //requireNonNull(request.amount(), "amount");

        PointAccrual accrual = new PointAccrual();

        accrual.pointKey = PointKeyGenerator.generatePointKey(request.userId());
        accrual.userId = request.userId();
        accrual.amount = request.amount();
        accrual.remainAmount = request.amount();

        accrual.sourceType = request.sourceType();
        accrual.sourceId = request.sourceId();

        LocalDateTime today = LocalDateTime.now();
        accrual.expireAt = today.plusDays(request.expiryDays());
        accrual.createdAt = today;
        accrual.status = AccrualStatus.ACTIVE.name();


        return accrual;
    }

    public boolean isExpired(LocalDateTime now) {
        return expireAt != null && now.isAfter(expireAt);
    }

    public void allocate(long amount) {
        if (amount < 0 || amount > remainAmount) throw new IllegalArgumentException("invalid allocation");
        this.remainAmount -= amount;
        this.updatedAt = LocalDateTime.now();
    }

    public void restore(long amount) {
        this.remainAmount += amount;
        this.updatedAt = LocalDateTime.now();
    }
}