
package com.musinsa.freepoint.adapters.out.persistence;

import com.musinsa.freepoint.domain.accrual.PointAccrual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PointAccrualRepository extends JpaRepository<PointAccrual, String> {
    @Query("""
        SELECT a FROM PointAccrual a
        WHERE a.userId = :userId AND a.status = 'ACTIVE' AND a.remainAmount > 0 AND a.expireAt > CURRENT_DATE
        ORDER BY a.manual DESC, a.expireAt ASC, a.pointKey ASC
    """)
    List<PointAccrual> findUsableByUserId(@Param("userId") String userId);
}
