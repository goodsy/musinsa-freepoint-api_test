
package com.musinsa.freepoint.domain.usage;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Data
@Table(name = "point_usage_detail")
public class PointUsageDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long usageId;
    private String pointKey;
    private long amount;
    private long canceledAmount;
    private boolean expired;
    private String rePointKey;


    public static PointUsageDetail create(Long usageId, String pointKey, long amount) {
        PointUsageDetail detail = new PointUsageDetail();
        detail.usageId = usageId;
        detail.pointKey = pointKey;
        detail.amount = amount;
        return detail;
    }

    public static PointUsageDetail create(Long usageId, String pointKey, long amount,  boolean expired) {
        PointUsageDetail detail = create(usageId, pointKey, amount);
        detail.expired = expired;
        return detail;
    }

    // 취소 금액 누적
    public void addCanceledAmount(long amount) {
        this.canceledAmount += amount;
    }




}
