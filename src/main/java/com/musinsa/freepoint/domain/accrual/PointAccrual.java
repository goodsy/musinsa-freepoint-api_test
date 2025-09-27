
package com.musinsa.freepoint.domain.accrual;

import com.musinsa.freepoint.adapters.in.web.dto.AccrualRequest;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Version
    private int version;

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
        accrual.manual = request.manual();

        LocalDateTime today = LocalDateTime.now();
        accrual.expireAt = today.plusDays(request.expiryDays());
        accrual.updatedAt = today;
        accrual.createdAt = today;
        accrual.status = AccrualStatus.ACTIVE.name();


        return accrual;
    }

    public static PointAccrual createReversal(String userId, long amount, String sourceId, int expiryDays) {
        return PointAccrual.builder()
                .pointKey(PointKeyGenerator.generatePointKey(userId))
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

    /*public static PointAccrual createReversal(String userId, long amount) {
        return PointAccrual.builder()
                .pointKey(PointKeyGenerator.generatePointKey(userId))
                .userId(userId)
                .amount(amount)
                .remainAmount(amount)
                .manual(false)
                .sourceType(sourceType)
                .sourceId(sourceId)
                .expireAt(LocalDateTime.now())
                .status(AccrualStatus.ACTIVE.name())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }*/

    public boolean isActive() {
        return this.status == AccrualStatus.ACTIVE.name();
    }

    public boolean isExceedAmount(long amount) {
        return this.amount < amount ;
    }

    public boolean isExpired() {
        return expireAt != null && LocalDateTime.now().isAfter(expireAt);
    }

    public void cancel() {
        if (this.remainAmount != this.amount) {
            throw new IllegalStateException("이미 일부가 사용된 적립금은 취소할 수 없습니다.");
        }
        this.status = AccrualStatus.CANCELED.name();
        this.updatedAt = LocalDateTime.now();
    }

    public void use(long amount) {
        if (amount < 0 || amount > remainAmount) throw new IllegalArgumentException("잔액 부족");
        this.remainAmount -= amount;
        this.updatedAt = LocalDateTime.now();
    }

    public void restore(long amount) {
        this.remainAmount += amount;
        this.updatedAt = LocalDateTime.now();
    }
}