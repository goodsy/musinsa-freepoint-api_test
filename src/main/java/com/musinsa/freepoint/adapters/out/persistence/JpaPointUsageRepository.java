
package com.musinsa.freepoint.adapters.out.persistence;
import com.musinsa.freepoint.domain.usage.PointUsage;
import org.springframework.data.jpa.repository.JpaRepository;
public interface JpaPointUsageRepository extends JpaRepository<PointUsage, Long> { }
