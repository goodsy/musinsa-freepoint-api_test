package com.musinsa.freepoint.application.service;

import com.musinsa.freepoint.adapters.in.web.dto.AccrualRequest;
import com.musinsa.freepoint.adapters.out.persistence.JpaPointAccrualRepository;
import com.musinsa.freepoint.adapters.out.persistence.JpaPointWalletRepository;
import com.musinsa.freepoint.application.port.in.AccrualCommandPort.CreateAccrual;
import com.musinsa.freepoint.domain.accrual.PointAccrual;
import com.musinsa.freepoint.domain.wallet.PointWallet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccrualUseCaseTest {

    @Mock
    JpaPointAccrualRepository accrualRepo;

    @Mock
    JpaPointWalletRepository walletRepo;

    @Mock
    PointPolicyService pointPolicyService;

    @InjectMocks
    AccrualUseCase accrualUseCase;

    // 성공 케이스: 한도/만료/지갑 모두 정상, 기존 지갑 존재
    @Test
    void accrue_success_savesAccrual_and_updatesWallet() {
        String userId = "user-ok";
        long amount = 500L;
        int expiryDays = 7; // 현재 구현상 expiryDays > 0 이면 default를 쓰지만, validate는 요청값을 검사하므로, default(예: 30) 이상만 아니면 통과

        // 정책 스텁
        when(pointPolicyService.minAccrualPerTxn()).thenReturn(1L);
        when(pointPolicyService.maxAccrualPerTxn()).thenReturn(1_000_000L);
        when(pointPolicyService.defaultExpiryDays()).thenReturn(30);
        when(pointPolicyService.maxWalletBalanceFor(userId)).thenReturn(10_000L);

        // 기존 지갑 있음
        PointWallet wallet = mock(PointWallet.class);
        when(wallet.getTotalBalance()).thenReturn(100L);
        when(walletRepo.findById(userId)).thenReturn(Optional.of(wallet));

        // save는 그대로 인자로 받은 객체 반환
        when(accrualRepo.save(any(PointAccrual.class))).thenAnswer(inv -> inv.getArgument(0));
        when(walletRepo.save(any(PointWallet.class))).thenAnswer(inv -> inv.getArgument(0));

        AccrualRequest req = new AccrualRequest(userId, amount, expiryDays, false, "ORDER", "ORD-1");
        CreateAccrual cmd = new CreateAccrual(req);

        PointAccrual saved = accrualUseCase.accrue(cmd);

        assertNotNull(saved);

        // accrual 저장 검증
        ArgumentCaptor<PointAccrual> accrualCap = ArgumentCaptor.forClass(PointAccrual.class);
        verify(accrualRepo, times(1)).save(accrualCap.capture());
        assertEquals(userId, accrualCap.getValue().getUserId());
        assertEquals(amount, accrualCap.getValue().getAmount());

        // wallet 증가 및 저장 검증
        verify(wallet, times(1)).increase(amount, false);
        verify(walletRepo, times(1)).save(wallet);
    }

    // 실패: 금액이 최소 미만
    @Test
    void accrue_throw_whenAmountBelowMin() {
        String userId = "user-min";
        long amount = 50L; // min=100보다 작게

        when(pointPolicyService.minAccrualPerTxn()).thenReturn(100L);
        when(pointPolicyService.maxAccrualPerTxn()).thenReturn(1_000_000L);
        when(pointPolicyService.defaultExpiryDays()).thenReturn(30);
        when(pointPolicyService.maxWalletBalanceFor(userId)).thenReturn(10_000L);

        // 지갑 존재(잔액 0)
        PointWallet wallet = mock(PointWallet.class);
        when(wallet.getTotalBalance()).thenReturn(0L);
        when(walletRepo.findById(userId)).thenReturn(Optional.of(wallet));

        AccrualRequest req = new AccrualRequest(userId, amount, 1, false, "ORDER", "ORD-2");
        CreateAccrual cmd = new CreateAccrual(req);

        assertThrows(IllegalArgumentException.class, () -> accrualUseCase.accrue(cmd));
        verify(accrualRepo, never()).save(any());
        verify(walletRepo, never()).save(any());
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 실패: 금액이 최대 초과
    // ─────────────────────────────────────────────────────────────────────────────
    @Test
    void accrue_throw_whenAmountAboveMax() {
        String userId = "user-max";
        long amount = 5000L; // max=1000보다 크다

        when(pointPolicyService.minAccrualPerTxn()).thenReturn(1L);
        when(pointPolicyService.maxAccrualPerTxn()).thenReturn(1000L);
        when(pointPolicyService.defaultExpiryDays()).thenReturn(30);
        when(pointPolicyService.maxWalletBalanceFor(userId)).thenReturn(10_000L);

        PointWallet wallet = mock(PointWallet.class);
        when(wallet.getTotalBalance()).thenReturn(0L);
        when(walletRepo.findById(userId)).thenReturn(Optional.of(wallet));

        AccrualRequest req = new AccrualRequest(userId, amount, 1, false, "ORDER", "ORD-3");
        CreateAccrual cmd = new CreateAccrual(req);

        assertThrows(IllegalArgumentException.class, () -> accrualUseCase.accrue(cmd));
        verify(accrualRepo, never()).save(any());
        verify(walletRepo, never()).save(any());
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 실패: 만료일이 정책 기준 초과
    // 현재 구현:
    //   - resolveExpiryDays(expiryDays): >0 이면 default, <=0 이면 1
    //   - validateExpiryDays(expiryDays): (requested > resolve(requested)) 이면 예외
    // 따라서 default=30일 때, 요청 31 → 31 > 30 이므로 예외
    // ─────────────────────────────────────────────────────────────────────────────
    @Test
    void accrue_throw_whenExpiryDaysTooLarge() {
        String userId = "user-exp";
        long amount = 100L;
        int requestedExpiryDays = 31; // default=30보다 크게

        when(pointPolicyService.minAccrualPerTxn()).thenReturn(1L);
        when(pointPolicyService.maxAccrualPerTxn()).thenReturn(1_000_000L);
        when(pointPolicyService.defaultExpiryDays()).thenReturn(30);
        when(pointPolicyService.maxWalletBalanceFor(userId)).thenReturn(10_000L);

        PointWallet wallet = mock(PointWallet.class);
        when(wallet.getTotalBalance()).thenReturn(0L);
        when(walletRepo.findById(userId)).thenReturn(Optional.of(wallet));

        AccrualRequest req = new AccrualRequest(userId, amount, requestedExpiryDays, false, "ORDER", "ORD-4");
        CreateAccrual cmd = new CreateAccrual(req);

        assertThrows(IllegalArgumentException.class, () -> accrualUseCase.accrue(cmd));
        verify(accrualRepo, never()).save(any());
        verify(walletRepo, never()).save(any());
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 실패: 개인 지갑 보유 한도 초과
    // ─────────────────────────────────────────────────────────────────────────────
    @Test
    void accrue_throw_whenWalletBalanceExceeded() {
        String userId = "user-limit";
        long amount = 500L;

        when(pointPolicyService.minAccrualPerTxn()).thenReturn(1L);
        when(pointPolicyService.maxAccrualPerTxn()).thenReturn(1_000_000L);
        when(pointPolicyService.defaultExpiryDays()).thenReturn(30);
        when(pointPolicyService.maxWalletBalanceFor(userId)).thenReturn(10_000L);

        PointWallet wallet = mock(PointWallet.class);
        when(wallet.getTotalBalance()).thenReturn(9_800L); // 9800 + 500 = 10300 > 10000
        when(walletRepo.findById(userId)).thenReturn(Optional.of(wallet));

        AccrualRequest req = new AccrualRequest(userId, amount, 1, false, "ORDER", "ORD-5");
        CreateAccrual cmd = new CreateAccrual(req);

        assertThrows(IllegalArgumentException.class, () -> accrualUseCase.accrue(cmd));
        verify(accrualRepo, never()).save(any());
        verify(walletRepo, never()).save(any());
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 성공: 지갑이 없을 때 새로 생성되는 경로
    // (PointWallet.create(userId) 호출 경로)
    // ─────────────────────────────────────────────────────────────────────────────
    @Test
    void accrue_success_createsNewWallet_whenNotExists() {
        String userId = "user-new";
        long amount = 300L;
        int requestedExpiryDays = 5;

        when(pointPolicyService.minAccrualPerTxn()).thenReturn(1L);
        when(pointPolicyService.maxAccrualPerTxn()).thenReturn(1_000_000L);
        when(pointPolicyService.defaultExpiryDays()).thenReturn(30);
        when(pointPolicyService.maxWalletBalanceFor(userId)).thenReturn(10_000L);

        // 지갑 없음 → 새로 생성 경로
        when(walletRepo.findById(userId)).thenReturn(Optional.empty());
        // save 동작
        when(accrualRepo.save(any(PointAccrual.class))).thenAnswer(inv -> inv.getArgument(0));
        when(walletRepo.save(any(PointWallet.class))).thenAnswer(inv -> inv.getArgument(0));

        AccrualRequest req = new AccrualRequest(userId, amount, requestedExpiryDays, false, "ORDER", "ORD-6");
        CreateAccrual cmd = new CreateAccrual(req);

        PointAccrual saved = accrualUseCase.accrue(cmd);
        assertNotNull(saved);

        // 새 지갑 저장이 1회 이상 수행되었는지(구체 검증이 어렵다면 호출 여부만 확인)
        verify(walletRepo, atLeastOnce()).save(any(PointWallet.class));
        verify(accrualRepo, times(1)).save(any(PointAccrual.class));
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 경계값: 요청 만료일 <= 0 이면 현재 구현상 resolveExpiryDays가 1을 반환
    // (만료일 기본값이 아닌 1일이 적용되는 현재 코드의 동작을 고정)
    // ─────────────────────────────────────────────────────────────────────────────
    @Test
    void accrue_success_whenRequestedExpiryIsZeroOrNegative_resolvesToOneDay() {
        String userId = "user-exp-0";
        long amount = 100L;
        int requestedExpiryDays = 0; // <=0

        when(pointPolicyService.minAccrualPerTxn()).thenReturn(1L);
        when(pointPolicyService.maxAccrualPerTxn()).thenReturn(1_000_000L);
        // defaultExpiryDays는 현재 구현상 >
    }
}