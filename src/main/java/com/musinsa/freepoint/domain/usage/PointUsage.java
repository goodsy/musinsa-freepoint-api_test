
package com.musinsa.freepoint.domain.usage;

import com.musinsa.freepoint.domain.KeyGenerator;
import com.musinsa.freepoint.domain.accrual.AccrualStatus;
import com.musinsa.freepoint.domain.accrual.PointAccrual;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@Data
@Table(name = "point_usage")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PointUsage {
    @Id
    @Column(name="usage_key", length = 50)
    private String usageKey;
    private String userId;
    private String orderNo;
    private long amount;
    private String status;
    private String idempotencyKey;
    private LocalDateTime usedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String reversalOfId;

    @OneToMany(mappedBy = "usageKey", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PointUsageDetail> details = new ArrayList<>();

    public static PointUsage create(String userId, String orderNo, long amount, String status) {

        LocalDateTime now = LocalDateTime.now();
        return PointUsage.builder()
                .usageKey(KeyGenerator.generateUsageId())
                .userId(userId)
                .orderNo(orderNo)
                .amount(amount)
                .status(status)
                .usedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();

    }

    public void addDetail(String pointKey, long amount) {
        PointUsageDetail detail = PointUsageDetail.create(this.usageKey, pointKey, amount);
        details.add(detail);
    }

}
