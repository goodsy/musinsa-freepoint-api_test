
package com.musinsa.freepoint.application.service;

import com.musinsa.freepoint.adapters.in.web.exception.ApiErrorCode;
import com.musinsa.freepoint.adapters.out.persistence.*;
import com.musinsa.freepoint.application.port.in.UsageCommandPort.*;
import com.musinsa.freepoint.domain.DomainException;
import com.musinsa.freepoint.domain.accrual.PointAccrual;
import com.musinsa.freepoint.domain.usage.UsageStatus;
import com.musinsa.freepoint.domain.usage.PointUsage;
import com.musinsa.freepoint.domain.usage.PointUsageDetail;
import com.musinsa.freepoint.domain.wallet.PointWallet;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Log4j2
@Service
public class UseagePointUseCase {
    private final PointAccrualRepository accrualRepository;
    private final PointUsageRepository usageRepository;
    private final PointUsageDetailRepository usageDetailRepository;
    private final PointWalletRepository walletRepository;

    private final PointPolicyService pointPolicyService;

    public UseagePointUseCase(PointAccrualRepository accrualRepository,
                              PointUsageRepository usageRepository,
                              PointUsageDetailRepository usageDetailRepository,
                              PointWalletRepository walletRepository,
                              PointPolicyService pointPolicyService) {
        this.accrualRepository = accrualRepository;
        this.usageRepository = usageRepository;
        this.usageDetailRepository = usageDetailRepository;
        this.walletRepository = walletRepository;
        this.pointPolicyService = pointPolicyService;
    }

    /**
     * 포인트 사용 처리
     * - 사용 가능한 적립금(수기 지급 우선, 만료일 오름차순)에서 차감
     * - 부족 시 예외 발생
     */
    @Transactional
    public PointUsage usage(UsagePoint usePoint) {

        String userId = usePoint.request().userId();
        long amount = usePoint.request().amount();
        String orderNo = usePoint.request().orderNo();

        // 1. 사용 가능한 적립금(수기 지급 우선, 만료일 오름차순) 조회
        List<PointAccrual> accruals = accrualRepository.findUsableByUserId(userId);
        accruals.sort(Comparator
                .comparing(PointAccrual::isManual).reversed()
                .thenComparing(PointAccrual::getExpireAt)
                .thenComparing(PointAccrual::getPointKey));

        if (accruals.isEmpty()) {
            throw new DomainException(ApiErrorCode.ACCRUAL_NOT_FOUND);
        }

        PointUsage usage = PointUsage.create(userId, orderNo, amount, UsageStatus.USED.name());

        long remain = amount;
        long usedFromManual = 0;

        for (PointAccrual accrual : accruals) {
            if (remain <= 0) break;

            long useAmount = Math.min(remain, accrual.getRemainAmount());

            if (useAmount > 0) {
                accrual.use(useAmount);
                usage.addDetail(accrual.getPointKey(), useAmount);
                if (accrual.isManual()) {
                    usedFromManual += useAmount;
                }
                remain -= useAmount;
            }
        }

        if (remain > 0) throw new DomainException(ApiErrorCode.POINT_BALANCE_INSUFFICIENT);

        usageRepository.save(usage);

        PointWallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new DomainException(ApiErrorCode.WALLET_NOT_FOUND));
        wallet.use(amount, usedFromManual);
        walletRepository.save(wallet);
        walletRepository.save(wallet);

        return usage;

    }

    /**
     * 포인트 사용 취소 처리 (부분/전체)
     * - 만료된 적립금은 신규 적립 처리
     * - 만료 전 적립금은 잔액 복구
     * - 상세내역에 만료/신규적립 정보 기록
     */
    @Transactional
    public PointUsage cancel(CancelUsagePoint cancelAccrual) {
        String userId = cancelAccrual.request().userId();
        String orderNo = cancelAccrual.request().orderNo();
        long cancelAmount = cancelAccrual.request().amount();

        // userId, orderNo로 사용내역 조회
        List<PointUsage> usages = usageRepository.findByUserIdAndOrderNo(userId, orderNo);
        if (usages.isEmpty()) {
            throw new DomainException(ApiErrorCode.POINT_USAGE_HISTORY_NOT_FOUND);
        }

        // 취소 내역 생성
        PointUsage cancelUsage = PointUsage.create(
                userId,
                orderNo,
                cancelAmount,
                UsageStatus.CANCELED.name()
        );

        long remain = cancelAmount;
        long restoreToManual = 0;

        // 여러 사용내역을 순회하며 취소 처리
        for (PointUsage usage : usages) {
            if (remain <= 0) break;

            List<PointUsageDetail> details = usageDetailRepository.findByUsageKey(usage.getUsageKey());
            for (PointUsageDetail detail : details) {
                if (remain <= 0) break;

                long useAmount = detail.getAmount();
                long canceledAmount = detail.getCanceledAmount();
                long canCancel = useAmount - canceledAmount;
                long toCancel = Math.min(remain, canCancel);

                if (toCancel > 0) {
                    PointAccrual accrual = accrualRepository.findById(detail.getPointKey())
                            .orElseThrow(() -> new DomainException(ApiErrorCode.ACCRUAL_NOT_FOUND));
                    log.info("Cancelling {} from Accrual {} isExpired {} ", toCancel, accrual.getPointKey(), accrual.isExpired());
                    if (accrual.isExpired()) {
                        PointAccrual newAccrual = PointAccrual.createReversal(accrual.getUserId(), toCancel, usage.getUsageKey(), pointPolicyService.defaultExpiryDays());
                        accrualRepository.save(newAccrual);

                        PointUsageDetail cancelDetail = PointUsageDetail.create(cancelUsage.getUsageKey(), newAccrual.getPointKey(), toCancel, true);
                        usageDetailRepository.save(cancelDetail);
                    } else {
                        accrual.restore(toCancel);

                        PointUsageDetail cancelDetail = PointUsageDetail.create(cancelUsage.getUsageKey(), detail.getPointKey(), toCancel, false);
                        usageDetailRepository.save(cancelDetail);
                    }

                    if (accrual.isManual()) {
                        restoreToManual += toCancel;
                    }

                    detail.addCanceledAmount(toCancel);
                    usageDetailRepository.save(detail);

                    remain -= toCancel;
                }
            }
        }

        if (remain > 0) throw new DomainException(ApiErrorCode.CANCEL_AMOUNT_EXCEEDS_ORIGINAL_USAGE);

        usageRepository.save(cancelUsage);

        PointWallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new DomainException(ApiErrorCode.WALLET_NOT_FOUND));
        wallet.restore(cancelAmount, restoreToManual);
        walletRepository.save(wallet);

        return cancelUsage;
    }
}
