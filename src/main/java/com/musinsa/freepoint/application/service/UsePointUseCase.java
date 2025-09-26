
package com.musinsa.freepoint.application.service;

import com.musinsa.freepoint.adapters.out.persistence.*;
import com.musinsa.freepoint.domain.accrual.PointAccrual;
import com.musinsa.freepoint.domain.usage.PointUsage;
import com.musinsa.freepoint.application.port.in.AccrualCommandPort.UseCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class UsePointUseCase {
    private final JpaPointAccrualRepository accrualRepo;
    private final JpaPointUsageRepository usageRepo;
    private final JpaPointUsageDetailRepository detailRepo;
    private final JpaPointWalletRepository walletRepo;

    public UsePointUseCase(JpaPointAccrualRepository accrualRepo, JpaPointUsageRepository usageRepo, JpaPointUsageDetailRepository detailRepo, JpaPointWalletRepository walletRepo) {
        this.accrualRepo = accrualRepo;
        this.usageRepo = usageRepo;
        this.detailRepo = detailRepo;
        this.walletRepo = walletRepo;
    }

    @Transactional
    public PointUsage use(UseCommand c) {
        List<PointAccrual> avail = accrualRepo.findAvailable(c.userId());

        long remain = c.amount();
        if (remain <= 0) throw new IllegalArgumentException("amount must be positive");
        PointUsage u = PointUsage.create(c.userId(), c.orderNo(), c.amount());
        usageRepo.save(u);

        for (PointAccrual a : new ArrayList<>(avail)) {
            if (remain == 0) break;
            long take = Math.min(a.getRemainAmount(), remain);
            a.allocate(take);
            accrualRepo.save(a);
            //u.addDetail(a.getId(), take);
            remain -= take;
        }

        if (remain > 0) throw new IllegalArgumentException("잔액 부족");
        var wallet = walletRepo.findById(c.userId()).orElseThrow();
        wallet.decrease(c.amount());
        walletRepo.save(wallet);
        usageRepo.save(u);
        return u;
    }
}
