package com.musinsa.freepoint.domain.policy;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "point_policy_rule")
public class PointPolicyRule {
    @EmbeddedId
    private PointPolicyRuleId id;

    private String policyValue;

    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void touchTime() {
        this.updatedAt = LocalDateTime.now();
    }

    public static PointPolicyRule of(String scope, String subjectKey, String policyKey, String policyValue) {
        PointPolicyRule rule = new PointPolicyRule();
        rule.id = new PointPolicyRuleId(scope, subjectKey, policyKey);
        rule.policyValue = policyValue;
        return rule;
    }
}
