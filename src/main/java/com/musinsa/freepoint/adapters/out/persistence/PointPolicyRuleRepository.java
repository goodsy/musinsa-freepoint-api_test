package com.musinsa.freepoint.adapters.out.persistence;


import com.musinsa.freepoint.domain.PointPolicyRule;
import com.musinsa.freepoint.domain.PointPolicyRuleId;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface  PolicyRuleRepository  extends JpaRepository<PointPolicyRule, PointPolicyRuleId> {

    /**
     * 특정 policy_key에 대해 USER > TIER > GLOBAL 우선순위로 1건 조회
     * tierCode가 null이면 TIER 조건은 제외.
     */
    @Query(value = """
      SELECT p.policy_value
        FROM policy_rule p
       WHERE p.policy_key = :policyKey
         AND (
               (p.scope = 'USER'   AND p.subject_key = :userId) OR
               (:tierCode IS NOT NULL AND p.scope = 'TIER' AND p.subject_key = :tierCode) OR
               (p.scope = 'GLOBAL' AND p.subject_key = '*')
             )
       ORDER BY CASE p.scope WHEN 'USER' THEN 1 WHEN 'TIER' THEN 2 ELSE 3 END
       LIMIT 1
    """, nativeQuery = true)
    Optional<String> findBestValue(@Param("policyKey") String policyKey,
                                   @Param("userId") String userId,
                                   @Param("tierCode") String tierCode);
}
