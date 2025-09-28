package com.musinsa.freepoint.domain.policy;

import jakarta.persistence.Column;
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
    @Column(name = "scope", length = 10, nullable = false, updatable = false)
    private String scope;

    @Column(name = "scope_id", length = 64, nullable = false, updatable = false)
    private String scopeId;

    @Column(name = "policy_key", length = 50, nullable = false, updatable = false)
    private String policyKey;
}
