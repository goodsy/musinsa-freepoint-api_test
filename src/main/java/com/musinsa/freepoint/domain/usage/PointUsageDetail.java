
package com.musinsa.freepoint.domain.usage;

import com.musinsa.freepoint.domain.KeyGenerator;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Setter
@Getter
@Data
@Table(name = "point_usage_detail")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PointUsageDetail {
    @Id
    @Column(name="usage_detail_key", length = 50)
    private String usageDetailKey;

    @Column(name="usage_key",nullable = false)
    private String usageKey;

    @Column(name="point_key", length = 100)
    private String pointKey;

    @Column(name = "amount", nullable = false)
    private long amount;

    @Column(name = "canceled_amount", nullable = false)
    private long canceledAmount;

    @Column(name="expired", nullable = false)
    private boolean expired;
    private String rePointKey;


    public static PointUsageDetail create(String usageKey, String pointKey, long amount) {

        return PointUsageDetail.builder()
                .usageDetailKey(KeyGenerator.generateUsageId())
                .usageKey(usageKey)
                .pointKey(pointKey)
                .amount(amount)
                .build();
    }

    public static PointUsageDetail create(String usageKey, String pointKey, long amount,  boolean expired) {
        PointUsageDetail detail = create(usageKey, pointKey, amount);
        detail.expired = expired;
        return detail;
    }

    // 취소 금액 누적
    public void addCanceledAmount(long amount) {
        this.canceledAmount += amount;
    }


}
