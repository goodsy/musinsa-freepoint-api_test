package com.musinsa.freepoint.domain;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PointPolicyRuleId  implements Serializable {
    private String scope;       // 'USER' | 'TIER' | 'GLOBAL'
    private String subjectKey;  // userId | tierCode | '*'
    private String policyKey;   // 'wallet.maxBalance' 등
}
