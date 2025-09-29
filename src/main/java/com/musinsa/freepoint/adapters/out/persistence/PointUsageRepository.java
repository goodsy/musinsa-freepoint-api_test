
package com.musinsa.freepoint.adapters.out.persistence;
import com.musinsa.freepoint.domain.usage.PointUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointUsageRepository extends JpaRepository<PointUsage, String> {
    List<PointUsage> findByUserIdAndOrderNo(String userId, String orderNo);
}
