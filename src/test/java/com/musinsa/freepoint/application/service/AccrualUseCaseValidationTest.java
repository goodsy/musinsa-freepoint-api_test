package com.musinsa.freepoint.application.service;

import com.musinsa.freepoint.adapters.in.web.dto.AccrualRequest;
import com.musinsa.freepoint.adapters.out.persistence.JpaPointAccrualRepository;
import com.musinsa.freepoint.adapters.out.persistence.JpaPointWalletRepository;
import com.musinsa.freepoint.application.port.in.AccrualCommandPort.CreateAccrual;
import com.musinsa.freepoint.domain.accrual.PointAccrual;
import com.musinsa.freepoint.domain.accural.AccrualRequestFactory;
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
class AccrualUseCaseValidationTest {

    @Mock
    private JpaPointAccrualRepository accrualRepo;

    @Mock
    private JpaPointWalletRepository walletRepo;

    @Mock
    private PointPolicyService pointPolicyService;

    @InjectMocks
    private AccrualUseCase accrualUseCase;

    // 성공 케이스
    @Test
    void accrue_success_savesAccrualAndUpdatesWallet() {
        String userId = "user-1";
        long amount = 500L;
        int expiryDays = 7;

        AccrualRequest request =
                AccrualRequestFactory.create(userId, amount, expiryDays, false, "ORDER", "order-123");
        CreateAccrual cmd = new CreateAccrual(request);

        when(pointPolicyService.minAccrualPerTxn()).thenReturn(1L);
        when(pointPolicyService.maxAccrualPerTxn()).thenReturn(1_000_000L);
        when(pointPolicyService.defaultExpiryDays()).thenReturn(30);
        when(pointPolicyService.maxWalletBalanceFor(userId)).thenReturn(10_000L);

        PointWallet wallet = mock(PointWallet.class);
        when(wallet.getTotalBalance()).thenReturn(0L);
        when(walletRepo.findById(userId)).thenReturn(Optional.of(wallet));
        when(accrualRepo.save(any(PointAccrual.class))).thenAnswer(inv -> inv.getArgument(0));
        when(walletRepo.save(any(PointWallet.class))).thenAnswer(inv -> inv.getArgument(0));

        PointAccrual result = accrualUseCase.accrue(cmd);

        assertNotNull(result);
        ArgumentCaptor<PointAccrual> captor = ArgumentCaptor.forClass(PointAccrual.class);
        verify(accrualRepo, times(1)).save(captor.capture());
        verify(wallet).increase(amount, false);
        verify(walletRepo, times(1)).save(wallet);
        assertEquals(amount, captor.getValue().getAmount());
    }

    // 금액이 최소값보다 작은 경우
    @Test
    void accrue_throwsWhenAmountBelowMin() {
        String userId = "user-2";
        long amount = 50L;

        AccrualRequest request = new AccrualRequest(userId, amount, 1, false, "ORDER", "order-xyz");
        CreateAccrual cmd = new CreateAccrual(request);

        when(pointPolicyService.minAccrualPerTxn()).thenReturn(100L);
        when(pointPolicyService.maxAccrualPerTxn()).thenReturn(1_000_000L);


        assertThrows(IllegalArgumentException.class, () -> accrualUseCase.accrue(cmd));
        verify(accrualRepo, never()).save(any());
    }

    // 금액이 최대값보다 큰 경우
    @Test
    void accrue_throwsWhenAmountAboveMax() {
        String userId = "user-3";
        long amount = 5_000L;

        AccrualRequest request = new AccrualRequest(userId, amount, 1,false, "ORDER", "order-big");
        CreateAccrual cmd = new CreateAccrual(request);

        when(pointPolicyService.minAccrualPerTxn()).thenReturn(1L);
        when(pointPolicyService.maxAccrualPerTxn()).thenReturn(1_000L);


        assertThrows(IllegalArgumentException.class, () -> accrualUseCase.accrue(cmd));
        verify(accrualRepo, never()).save(any());
    }

    // 지갑 보유 한도 초과 케이스
    @Test
    void accrue_throwsWhenWalletBalanceExceeded() {
        String userId = "user-5";
        long amount = 500L;

        AccrualRequest request = new AccrualRequest(userId, amount, 1,false, "ORDER", "order-limit");
        CreateAccrual cmd = new CreateAccrual(request);

        when(pointPolicyService.minAccrualPerTxn()).thenReturn(1L);
        when(pointPolicyService.maxAccrualPerTxn()).thenReturn(1_000_000L);
        when(pointPolicyService.maxWalletBalanceFor(userId)).thenReturn(10_000L);

        PointWallet wallet = mock(PointWallet.class);
        when(wallet.getTotalBalance()).thenReturn(9_800L);
        when(walletRepo.findById(userId)).thenReturn(Optional.of(wallet));

        assertThrows(IllegalArgumentException.class, () -> accrualUseCase.accrue(cmd));
        verify(accrualRepo, never()).save(any());
    }

    // 만료일이 정책의 최대값을 초과하는 경우
    @Test
    void accrue_throwsWhenExpiryDaysTooLarge() {
        String userId = "user-4";
        long amount = 100L;
        int expiryDays = 31;

        AccrualRequest request = new AccrualRequest(userId, amount, expiryDays,false, "ORDER", "order-exp");
        CreateAccrual cmd = new CreateAccrual(request);

        when(pointPolicyService.minAccrualPerTxn()).thenReturn(1L);
        when(pointPolicyService.maxAccrualPerTxn()).thenReturn(1_000_000L);
        when(pointPolicyService.maxWalletBalanceFor(userId)).thenReturn(10_000L);
        when(pointPolicyService.defaultExpiryDays()).thenReturn(30);

        PointWallet wallet = mock(PointWallet.class);
        when(wallet.getTotalBalance()).thenReturn(0L);
        when(walletRepo.findById(userId)).thenReturn(Optional.of(wallet));

        assertThrows(IllegalArgumentException.class, () -> accrualUseCase.accrue(cmd));
        verify(accrualRepo, never()).save(any());
    }
}
