
package com.musinsa.freepoint.domain.usage;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@Data
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
    private LocalDateTime usedAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    private Long reversalOfId;

    @OneToMany(mappedBy = "usageId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PointUsageDetail> details = new ArrayList<>();

    public static PointUsage create(String userId, String orderNo, long amount, String status) {
        PointUsage usage = new PointUsage();
        usage.userId = userId;
        usage.orderNo = orderNo;
        usage.amount = amount;
        usage.status = status;

        LocalDateTime today = LocalDateTime.now();
        usage.usedAt = today;
        usage.updatedAt = today;

        return usage;
    }

    public void addDetail(String pointKey, long amount) {
        details.add(PointUsageDetail.create(this.id, pointKey, amount));
    }

}
