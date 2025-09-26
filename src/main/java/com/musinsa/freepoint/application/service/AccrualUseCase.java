
package com.musinsa.freepoint.application.service;

import com.musinsa.freepoint.adapters.out.persistence.JpaPointAccrualRepository;
import com.musinsa.freepoint.adapters.out.persistence.JpaPointWalletRepository;
import com.musinsa.freepoint.application.port.in.AccrualCommandPort.CreateAccrual;
import com.musinsa.freepoint.domain.accrual.PointAccrual;
import com.musinsa.freepoint.domain.wallet.PointWallet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AccrualUseCase {
    private final JpaPointAccrualRepository accrualRepo;
    private final JpaPointWalletRepository walletRepo;
    private final PointPolicyService pointPolicyService;


    @Transactional
    public PointAccrual accrue(CreateAccrual cmd) {

        //1) 적립 가능 금액 범위 체크
        validateAccrualAmount(cmd.request().amount());

        //2) 개인별 최대 보유 가능 금액 체크
        PointWallet wallet = walletRepo.findById(cmd.request().userId()).orElse(PointWallet.create(cmd.request().userId()));
        validateWalletBalance(cmd.request().userId(), wallet.getTotalBalance(), cmd.request().amount());

        //3) 만료일 범위 체크
        int expiryDays = resolveExpiryDays(cmd.request().expiryDays());
        validateExpiryDays(cmd.request().expiryDays());


        LocalDateTime expiresAt = LocalDateTime.now().plus(expiryDays, ChronoUnit.DAYS);
        PointAccrual a = PointAccrual.create(cmd.request().userId(), cmd.request().amount(), cmd.request().manual(), cmd.request().sourceType(), cmd.request().sourceId(), expiresAt);

        accrualRepo.save(a);

        wallet.increase(cmd.request().amount(), cmd.request().manual());
        walletRepo.save(wallet);
        return a;
    }

    private void validateAccrualAmount(long amount) {
        long minAmount = pointPolicyService.minAccrualPerTxn();
        long maxAmount = pointPolicyService.maxAccrualPerTxn();

        if (amount < minAmount || amount > maxAmount)
            throw new IllegalArgumentException("1회 적립 한도 위반[적립 가능 금액="+minAmount+"~"+maxAmount+"]");
    }

    private int resolveExpiryDays(int expiryDays) {
        return expiryDays <= 0 ? 1 : pointPolicyService.defaultExpiryDays();
    }

    private void validateExpiryDays(int expiryDays) {
        int maxExpiryDays = resolveExpiryDays(expiryDays);
        if (expiryDays > maxExpiryDays)
            throw new IllegalArgumentException("만료일 범위 위반[유효시간 최대 일=" + maxExpiryDays + "]");
    }

    private void validateWalletBalance(String userId, long currentBalance, long accrual) {
        long maxWalletBalance = pointPolicyService.maxWalletBalanceFor(userId);
        if (currentBalance + accrual > maxWalletBalance)
            throw new IllegalArgumentException("보유 한도 초과[최대 가능 금액="+maxWalletBalance+"]");
    }

}
