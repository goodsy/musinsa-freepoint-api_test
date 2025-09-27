
package com.musinsa.freepoint.application.service;

import com.musinsa.freepoint.adapters.out.persistence.*;
import com.musinsa.freepoint.application.port.in.UsageCommandPort.*;
import com.musinsa.freepoint.domain.accrual.PointAccrual;
import com.musinsa.freepoint.domain.usage.UsageStatus;
import com.musinsa.freepoint.domain.usage.PointUsage;
import com.musinsa.freepoint.domain.usage.PointUsageDetail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

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


        PointUsage usage = PointUsage.create(userId, orderNo, amount, UsageStatus.USED.name());

        long remain = amount;
        for (PointAccrual accrual : accruals) {
            if (remain <= 0) break;

            long useAmount = Math.min(remain, accrual.getRemainAmount());
            if (useAmount > 0) {
                accrual.use(useAmount);
                usage.addDetail(accrual.getPointKey(), useAmount);
                remain -= useAmount;
            }
        }

        if (remain > 0) throw new IllegalArgumentException("포인트 잔액 부족");

        usageRepository.save(usage);

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
        long usageId = cancelAccrual.request().usageId();
        long cancelAmount = cancelAccrual.request().canceledAmount();

        PointUsage usage = usageRepository.findById(usageId)
                .orElseThrow(() -> new IllegalArgumentException("원본 사용 내역 없음"));

        List<PointUsageDetail> details = usageDetailRepository.findByUsageId(usageId);

        long remain = cancelAmount;

        PointUsage cancelUsage = PointUsage.create(
                usage.getUserId(),
                usage.getOrderNo(),
                cancelAmount,
                UsageStatus.CANCELED.name()
        );
        cancelUsage.setReversalOfId(usageId);

        for (PointUsageDetail detail : details) {

            if (remain <= 0) break;

            long useAmount = detail.getAmount();
            long canceledAmount = detail.getCanceledAmount(); // 이미 취소된 금액

            long canCancel = useAmount - canceledAmount;
            long toCancel = Math.min(remain, canCancel);

            if (toCancel > 0) {
                PointAccrual accrual = accrualRepository.findById(detail.getPointKey())
                        .orElseThrow(() -> new IllegalArgumentException("적립금 없음"));

                if (accrual.isExpired()) {
                    // 만료: 신규 적립 처리
                    PointAccrual newAccrual = PointAccrual.createReversal(accrual.getUserId(), toCancel, String.valueOf(usageId), pointPolicyService.defaultExpiryDays());
                    accrualRepository.save(newAccrual);

                    PointUsageDetail cancelDetail = PointUsageDetail.create(cancelUsage.getId(), newAccrual.getPointKey(), toCancel, true);
                    usageDetailRepository.save(cancelDetail);
                } else {
                    // 만료 전: 잔액 복구
                    accrual.restore(toCancel);

                    PointUsageDetail cancelDetail = PointUsageDetail.create(cancelUsage.getId(), detail.getPointKey(), toCancel, false);
                    usageDetailRepository.save(cancelDetail);
                }

                // 원본 상세내역에 취소 누적 (별도 필드 필요)
                detail.addCanceledAmount(toCancel);
                usageDetailRepository.save(detail);

                remain -= toCancel;
            }
        }

        if (remain > 0) throw new IllegalArgumentException("취소 금액이 원본 사용 내역을 초과");

        usageRepository.save(cancelUsage);
        return cancelUsage;
    }
}
