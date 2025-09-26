
package com.musinsa.freepoint.application.service;

import com.musinsa.freepoint.adapters.out.persistence.*;
import com.musinsa.freepoint.domain.usage.PointUsage;
import com.musinsa.freepoint.application.port.in.AccrualCommandPort.CancelUseCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UseCancelUseCase {
    private final JpaPointUsageRepository usageRepo;
    private final JpaPointUsageDetailRepository detailRepo;
    private final JpaPointAccrualRepository accrualRepo;
    private final JpaPointWalletRepository walletRepo;

    public UseCancelUseCase(JpaPointUsageRepository usageRepo, JpaPointUsageDetailRepository detailRepo, JpaPointAccrualRepository accrualRepo, JpaPointWalletRepository walletRepo) {
        this.usageRepo = usageRepo;
        this.detailRepo = detailRepo;
        this.accrualRepo = accrualRepo;
        this.walletRepo = walletRepo;
    }

    @Transactional
    public PointUsage cancel(CancelUseCommand c) {
        PointUsage u = usageRepo.findById(c.usageId()).orElseThrow();
        /*PointUsage u = usageRepo.findById(c.usageId()).orElseThrow();
        long toCancel = c.amount();
        if (toCancel <= 0) throw new IllegalArgumentException("cancel amount must be positive");
        List<PointUsageDetail> details = detailRepo.findByUsageIdOrderByIdDesc(u.getId());
        Instant now = Instant.now();
        for (PointUsageDetail d : details) {
            if (toCancel == 0) break;
            long giveBack = Math.min(d.getAmount(), toCancel);
            PointAccrual src = accrualRepo.findById(d.getAccrualId()).orElseThrow();
            if (src.isExpired(now)) {
                PointAccrual rev = PointAccrual.create(u.getUserId(), giveBack, false, "REVERSAL", "USAGE_CANCEL", now.plusSeconds(365 * 86400L));
                accrualRepo.save(rev);
            } else {
                src.restore(giveBack);
                accrualRepo.save(src);
            }
            toCancel -= giveBack;
        }
        var wallet = walletRepo.findById(u.getUserId()).orElseThrow();
        wallet.increase(c.amount(), false);
        walletRepo.save(wallet);*/
        return u;
    }
}
