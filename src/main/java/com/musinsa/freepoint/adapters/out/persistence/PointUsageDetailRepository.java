
package com.musinsa.freepoint.adapters.out.persistence;
import com.musinsa.freepoint.domain.usage.PointUsageDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface PointUsageDetailRepository extends JpaRepository<PointUsageDetail, String> {
    List<PointUsageDetail> findByUsageKey(String usageKey);
}
