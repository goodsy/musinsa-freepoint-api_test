
package com.musinsa.freepoint.domain.usage;
import jakarta.persistence.*;
@Entity @Table(name = "point_usage_detail")
public class PointUsageDetail {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long usageId; private Long accrualId; private long amount;
    public static PointUsageDetail of(Long usageId, Long accrualId, long amount){ PointUsageDetail d = new PointUsageDetail(); d.usageId=usageId; d.accrualId=accrualId; d.amount=amount; return d; }
    public Long getUsageId(){ return usageId; } public Long getAccrualId(){ return accrualId; } public long getAmount(){ return amount; }
}
