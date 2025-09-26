
package com.musinsa.freepoint.application.service;

import com.musinsa.freepoint.adapters.out.persistence.JpaPointAccrualRepository;
import com.musinsa.freepoint.adapters.out.persistence.JpaPointWalletRepository;
import com.musinsa.freepoint.application.port.in.Commands.AccrualCommand;
import com.musinsa.freepoint.config.PolicyConfig;
import com.musinsa.freepoint.domain.accrual.PointAccrual;
import com.musinsa.freepoint.domain.wallet.PointWallet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class AccrualUseCase {
    private final JpaPointAccrualRepository accrualRepo;
    private final JpaPointWalletRepository walletRepo;
    private final PolicyConfig policy;

    public AccrualUseCase(JpaPointAccrualRepository accrualRepo, JpaPointWalletRepository walletRepo, PolicyConfig policy) {
        this.accrualRepo = accrualRepo;
        this.walletRepo = walletRepo;
        this.policy = policy;
    }

    @Transactional
    public PointAccrual accrue(AccrualCommand cmd) {

        validateAccrualAmount(cmd.request().amount(), policy);

        int expiry = resolveExpiryDays(cmd.request().expiryDays(), policy);
        validateExpiryDays(expiry, policy);

        PointWallet wallet = walletRepo.findById(cmd.request().userId()).orElse(new PointWallet(cmd.request().userId()));
        validateWalletBalance(wallet.getTotalBalance(), cmd.request().amount(), policy);

        LocalDateTime expiresAt = LocalDateTime.now().plus(expiry, ChronoUnit.DAYS);
        PointAccrual a = PointAccrual.create(cmd.request().userId(), cmd.request().amount(), cmd.request().manual(), cmd.request().sourceType(), cmd.request().sourceId(), expiresAt);

        accrualRepo.save(a);

        wallet.increase(cmd.request().amount(), cmd.request().manual());
        walletRepo.save(wallet);
        return a;
    }

    private void validateAccrualAmount(long amount, PolicyConfig policy) {
        if (amount < 1 || amount > policy.getMaxAccrualPerTxn())
            throw new IllegalArgumentException("1회 적립 한도 위반.[최소 금액="+policy.getMaxAccrualPerTxn()+"]");
    }

    private int resolveExpiryDays(int expiryDays, PolicyConfig policy) {
        return expiryDays < 0 ? expiryDays : policy.getDefaultExpiryDays();
    }

    private void validateExpiryDays(int expiry, PolicyConfig policy) {
        if (expiry < policy.getMinExpiryDays() || expiry > policy.getMaxExpiryDays())
            throw new IllegalArgumentException("만료일 범위 위반[" + policy.getMinExpiryDays() + "~" + policy.getMaxExpiryDays() + "]");
    }

    private void validateWalletBalance(long currentBalance, long accrual, PolicyConfig policy) {
        if (currentBalance + accrual > policy.getMaxWalletBalance())
            throw new IllegalArgumentException("보유 한도 초과[최대 가능 금액="+policy.getMaxWalletBalance()+"]");
    }


/*    public static void validateOneTimeAccrualLimit(long amount, AccrualPolicy policy){
        if (amount < policy.minPerOnce())
            throw new IllegalArgumentException("최소 적립 가능한 금액이 아닙니다.[최소 금액="+policy.minPerOnce()+"]");
        if (amount > policy.maxPerOnce())
            throw new IllegalArgumentException("적립가능한 최대 포인트 금액을 초과하였습니다.[최대 금액="+policy.maxPerOnce()+"]");
    }

    private static void validateExpirationDays(Integer expireDays, AccrualPolicy policy) {

        int days = (expireDays != null) ? expireDays : policy.defaultExpireDays();
        if (days < policy.minExpireDays() || days > policy.maxExpireDays())
            throw new IllegalArgumentException("부여가능한 만료일이 아닙니다. [" + policy.minExpireDays() + "~" + policy.maxExpireDays() + "]");

    }

    private void validateMaxHoldingLimit(long amount, String userId, AccrualPolicy policy) {
        LocalDateTime now = LocalDateTime.now();
        long remainAmount = accrualFingerPort.findRemainAmount(userId, now).value();
        if (remainAmount + amount > policy.maxBalancePerUser())
            throw new IllegalArgumentException("개인별 최대 보유가능한 포인트 금액을 초과하였습니다.[최대 가능 금액="+policy.maxBalancePerUser()+"]");

    }*/
}
