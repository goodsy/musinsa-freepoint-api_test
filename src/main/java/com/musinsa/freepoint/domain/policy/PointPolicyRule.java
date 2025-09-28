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

    @Column(nullable = false)
    private String policyValue;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void touchTime() {
        this.updatedAt = LocalDateTime.now();
    }

}
