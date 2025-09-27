
package com.musinsa.freepoint.application.service;

import com.musinsa.freepoint.adapters.in.web.dto.AccrualRequest;
import com.musinsa.freepoint.adapters.out.persistence.PointAccrualRepository;
import com.musinsa.freepoint.adapters.out.persistence.PointWalletRepository;
import com.musinsa.freepoint.application.port.in.AccrualCommandPort.CreateAccrual;
import com.musinsa.freepoint.application.port.in.AccrualCommandPort.CancelAccrual;
import com.musinsa.freepoint.domain.accrual.PointAccrual;
import com.musinsa.freepoint.domain.wallet.PointWallet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccrualUseCase {
    private final PointAccrualRepository accrualRepo;
    private final PointWalletRepository walletRepo;
    private final PointPolicyService pointPolicyService;


    @Transactional
    public PointAccrual accrue(CreateAccrual accrual) {

        AccrualRequest request = accrual.request();
        String userId = request.userId();
        Long amount = request.amount();

        //1) 적립 가능 금액 범위 체크
        validateAccrualAmount(amount);

        //2) 개인별 최대 보유 가능 금액 체크
        PointWallet wallet = walletRepo.findById(userId).orElse(PointWallet.create(userId));
        validateWalletBalance(userId, wallet.getTotalBalance(), amount);

        //3) 만료일 범위 체크
        int resolvedExpiryDays = resolveExpiryDays(request.expiryDays()); //요청값이 안들아감..
        validateExpiryDays(resolvedExpiryDays);

        PointAccrual pointAccrual = PointAccrual.create(request);

        accrualRepo.save(pointAccrual);

        wallet.increase(request.amount(), request.manual());
        walletRepo.save(wallet);
        return pointAccrual;
    }

    @Transactional
    public PointAccrual cancelAccrual(CancelAccrual cancelAccrual) {

        String userId = cancelAccrual.request().userId();
        String pointKey = cancelAccrual.request().pointKey();
        long cancelAmount = cancelAccrual.request().amount();


        PointAccrual accrual = accrualRepo.findById(pointKey)
                .orElseThrow(() -> new IllegalArgumentException("적립 내역이 존재하지 않습니다."));

        if (!accrual.getUserId().equals(accrual.getUserId())) {
            throw new IllegalArgumentException("본인 적립만 취소할 수 있습니다.");
        }

        // 취소 금액이 적립 금액보다 큰 경우 예외 처리
        if (accrual.isExceedAmount(cancelAmount)) {
            throw new IllegalArgumentException("적립 금액보다 큰 금액은 취소할 수 없습니다.");
        }

        if (!accrual.isActive()) {
            throw new IllegalArgumentException("적립 취소가 불가능한 건입니다.");
        }

        accrual.cancel();

        PointWallet wallet = walletRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("지갑이 존재하지 않습니다."));


        wallet.decrease(accrual.getAmount());
        walletRepo.save(wallet);
        accrualRepo.save(accrual);

        return accrual;
    }

    private void validateAccrualAmount(long amount) {
        long minAmount = pointPolicyService.minAccrualPerTxn();
        long maxAmount = pointPolicyService.maxAccrualPerTxn();

        if (amount < minAmount || amount > maxAmount)
            throw new IllegalArgumentException("1회 적립 한도 위반[적립 가능 금액="+minAmount+"~"+maxAmount+"]");
    }

    private int resolveExpiryDays(int expiryDays) {
        return expiryDays <= 0 ? pointPolicyService.defaultExpiryDays() : expiryDays;
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
