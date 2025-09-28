
package com.musinsa.freepoint.application.service;

import com.musinsa.freepoint.adapters.in.web.dto.AccrualRequest;
import com.musinsa.freepoint.adapters.in.web.exception.ApiErrorCode;
import com.musinsa.freepoint.adapters.out.persistence.PointAccrualRepository;
import com.musinsa.freepoint.adapters.out.persistence.PointWalletRepository;
import com.musinsa.freepoint.application.port.in.AccrualCommandPort.CreateAccrual;
import com.musinsa.freepoint.application.port.in.AccrualCommandPort.CancelAccrual;
import com.musinsa.freepoint.domain.DomainException;
import com.musinsa.freepoint.domain.accrual.PointAccrual;
import com.musinsa.freepoint.domain.wallet.PointWallet;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class AccrualUseCase {
    private static final Logger log = LogManager.getLogger(AccrualUseCase.class);
    private final PointAccrualRepository accrualRepository;
    private final PointWalletRepository walletRepository;
    private final PointPolicyService policyService;


    @Transactional
    public PointAccrual accrue(CreateAccrual accrual) {

        AccrualRequest request = accrual.request();
        String userId = request.userId();
        Long amount = request.amount();

        //1) 적립 가능 금액 범위 체크
        validateAccrualAmount(amount);

        //2) 개인별 최대 보유 가능 금액 체크
        PointWallet wallet = walletRepository.findById(userId).orElse(PointWallet.create(userId));
        validateWalletBalance(userId, wallet.getTotalBalance(), amount);

        //3) 만료일 범위 체크
        int resolvedExpiryDays = resolveExpiryDays(request.expiryDays());
        validateExpiryDays(resolvedExpiryDays);

        PointAccrual pointAccrual = PointAccrual.create(request);

        accrualRepository.save(pointAccrual);

        wallet.increase(request.amount(), request.manual());
        walletRepository.save(wallet);
        return pointAccrual;
    }

    @Transactional
    public PointAccrual cancelAccrual(CancelAccrual cancelAccrual) {

        String userId = cancelAccrual.request().userId();
        String pointKey = cancelAccrual.request().pointKey();
        //long cancelAmount = cancelAccrual.request().amount();


        PointAccrual accrual = accrualRepository.findById(pointKey)
                .orElseThrow(() -> new DomainException(ApiErrorCode.ACCRUAL_NOT_FOUND));

        if (!userId.equals(accrual.getUserId())) {
            throw new DomainException(ApiErrorCode.ACCRUAL_NOT_OWNED);
        }

        /*
        // 취소 금액이 적립 금액보다 큰 경우 예외 처리
        if (accrual.isExceedAmount(cancelAmount)) {
            throw new DomainException(ApiErrorCode.CANCEL_AMOUNT_EXCEEDS);
        }*/

        if (!accrual.isActive()) {
            throw new DomainException(ApiErrorCode.ACCRUAL_INACTIVE);
        }

        accrual.cancel();

        PointWallet wallet = walletRepository.findById(userId)
                .orElseThrow(() -> new DomainException(ApiErrorCode.WALLET_NOT_FOUND));


        wallet.decrease(accrual.getAmount());
        walletRepository.save(wallet);
        accrualRepository.save(accrual);

        return accrual;
    }

    private void validateAccrualAmount(long amount) {
        long minAmount = policyService.minAccrualPerTxn();
        long maxAmount = policyService.maxAccrualPerTxn();

        if (amount < minAmount || amount > maxAmount)
            throw DomainException.withFormat(
                    ApiErrorCode.INVALID_ACCRUAL_AMOUNT,
                    "(금액: %,d/ 한도: %,d~%,d)", amount, minAmount, maxAmount
            );
    }

    private int resolveExpiryDays(int expiryDays) {
        return expiryDays <= 0 ? policyService.defaultExpiryDays() : expiryDays;
    }

    private void validateExpiryDays(int expiryDays) {
        int maxExpiryDays = policyService.maxExpiryDays();
        if (expiryDays > maxExpiryDays)
            throw DomainException.withFormat(
                    ApiErrorCode.EXPIRY_POLICY_VIOLATION,
                    "(요청 만기일: %,d/ 최대 만기일: %,d)", expiryDays, maxExpiryDays
            );
    }

    private void validateWalletBalance(String userId, long currentBalance, long accrual) {
        long maxWalletBalance = policyService.maxWalletBalanceFor(userId);
        if (currentBalance + accrual > maxWalletBalance)
            throw DomainException.withFormat(
                    ApiErrorCode.MAX_HOLDING_EXCEEDED,
                    "(요청금액: %,d/ 현재잔액: %,d / 한도잔액: %,d)", accrual, currentBalance, maxWalletBalance
            );
    }

}
