
package com.musinsa.freepoint.domain.usage;

import com.musinsa.freepoint.domain.model.Enums.UsageStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "point_usage")
public class PointUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String userId;
    private String orderNo;
    private long amount;
    private String status;
    private String idempotencyKey;
    private Instant usedAt = Instant.now();
    private Instant updatedAt = Instant.now();
    @OneToMany(mappedBy = "usageId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PointUsageDetail> details = new ArrayList<>();

    public static PointUsage create(String userId, String orderNo, long amount) {
        PointUsage u = new PointUsage();
        u.userId = userId;
        u.orderNo = orderNo;
        u.amount = amount;
        u.status = UsageStatus.USED.name();
        return u;
    }

    public void addDetail(Long accrualId, long amount) {
        details.add(PointUsageDetail.of(this.id, accrualId, amount));
    }

    public Long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public long getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }
}
