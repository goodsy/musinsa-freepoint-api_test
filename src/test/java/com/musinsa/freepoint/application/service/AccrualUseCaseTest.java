package com.musinsa.freepoint.application.service;

import com.musinsa.freepoint.adapters.in.web.dto.AccrualRequest;
import com.musinsa.freepoint.adapters.out.persistence.PointAccrualRepository;
import com.musinsa.freepoint.adapters.out.persistence.PointWalletRepository;
import com.musinsa.freepoint.application.port.in.AccrualCommandPort.CreateAccrual;
import com.musinsa.freepoint.domain.accrual.PointAccrual;
import com.musinsa.freepoint.domain.accural.AccrualRequestFactory;
import com.musinsa.freepoint.domain.wallet.PointWallet;
import org.junit.jupiter.api.BeforeEach;
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
    PointAccrualRepository accrualRepo;

    @Mock
    PointWalletRepository walletRepo;

    @Mock
    PointPolicyService pointPolicyService;

    @InjectMocks
    AccrualUseCase accrualUseCase;

    String USER_ID;

    @BeforeEach
    void setUp() {
        USER_ID = "musinsaId";
    }


    // 정상 적립 및 기존 지갑 업데이트
    @Test
    void accrue_success_savesAccrual_and_updatesWallet() {
        long amount = 500L;
        int expiryDays = 7;

        when(pointPolicyService.minAccrualPerTxn()).thenReturn(1L);
        when(pointPolicyService.maxAccrualPerTxn()).thenReturn(1_000_000L);
        when(pointPolicyService.maxWalletBalanceFor(USER_ID)).thenReturn(10_000L);
        when(pointPolicyService.defaultExpiryDays()).thenReturn(30);

        PointWallet wallet = mock(PointWallet.class);
        when(wallet.getTotalBalance()).thenReturn(100L);
        when(walletRepo.findById(USER_ID)).thenReturn(Optional.of(wallet));

        when(accrualRepo.save(any(PointAccrual.class))).thenAnswer(inv -> inv.getArgument(0));
        when(walletRepo.save(any(PointWallet.class))).thenAnswer(inv -> inv.getArgument(0));

        //적립 요청
        AccrualRequest request = AccrualRequestFactory.createWithExpiry(USER_ID, amount, expiryDays);
        CreateAccrual cmd = new CreateAccrual(request);

        PointAccrual saved = accrualUseCase.accrue(cmd);

        assertNotNull(saved);

        ArgumentCaptor<PointAccrual> accrualCap = ArgumentCaptor.forClass(PointAccrual.class);
        verify(accrualRepo, times(1)).save(accrualCap.capture());
        assertEquals(USER_ID, accrualCap.getValue().getUserId());
        assertEquals(amount, accrualCap.getValue().getAmount());

        verify(wallet, times(1)).increase(amount, false);
        verify(walletRepo, times(1)).save(wallet);
    }

    // 신규 지갑 생성 및 적립 성공
    @Test
    void accrue_success_createsNewWallet_whenNotExists() {
        long amount = 2000L; // 최소 적립금액 이상으로 수정

        when(pointPolicyService.minAccrualPerTxn()).thenReturn(1_000L);
        when(pointPolicyService.maxAccrualPerTxn()).thenReturn(1_000_000L);
        when(pointPolicyService.defaultExpiryDays()).thenReturn(30);
        when(pointPolicyService.maxWalletBalanceFor(USER_ID)).thenReturn(10_000L);

        when(walletRepo.findById(USER_ID)).thenReturn(Optional.empty());
        when(accrualRepo.save(any(PointAccrual.class))).thenAnswer(inv -> inv.getArgument(0));
        when(walletRepo.save(any(PointWallet.class))).thenAnswer(inv -> inv.getArgument(0));

        AccrualRequest request = AccrualRequestFactory.createWithDefalut(USER_ID, amount);
        CreateAccrual cmd = new CreateAccrual(request);

        PointAccrual saved = accrualUseCase.accrue(cmd);
        assertNotNull(saved);

        verify(walletRepo, atLeastOnce()).save(any(PointWallet.class));
        verify(accrualRepo, times(1)).save(any(PointAccrual.class));
    }

    //1회 적립가능 포인트 최소 제한 위반
    @Test
    void accrue_throw_whenAmountBelowMin() {
        long amount = 50L;

        when(pointPolicyService.minAccrualPerTxn()).thenReturn(100L);

        AccrualRequest request = AccrualRequestFactory.createWithDefalut(USER_ID, amount);
        CreateAccrual cmd = new CreateAccrual(request);

        assertThrows(IllegalArgumentException.class, () -> accrualUseCase.accrue(cmd));
        verify(accrualRepo, never()).save(any());
        verify(walletRepo, never()).save(any());
    }

    //1회 적립가능 포인트 최대 제한 위반
    @Test
    void accrue_throw_whenAmountAboveMax() {
        long amount = 90_000L;

        when(pointPolicyService.maxAccrualPerTxn()).thenReturn(50_000L);

        AccrualRequest request = AccrualRequestFactory.createWithDefalut(USER_ID, amount);
        CreateAccrual cmd = new CreateAccrual(request);

        assertThrows(IllegalArgumentException.class, () -> accrualUseCase.accrue(cmd));
        verify(accrualRepo, never()).save(any());
        verify(walletRepo, never()).save(any());
    }

    // 만료일자 최대 제한 위반
    @Test
    void accrue_throw_whenExpiryDaysTooLarge() {
        long amount = 100L;
        int requestedExpiryDays = 31;

        // 아래 stubbing이 실제로 accrualUseCase.accrue(cmd) 내부에서 호출되지 않으면 삭제해야 함
        // when(pointPolicyService.maxExpiryDays()).thenReturn(30);

        AccrualRequest request = AccrualRequestFactory.createWithExpiry(USER_ID, amount, requestedExpiryDays);
        CreateAccrual cmd = new CreateAccrual(request);

        assertThrows(IllegalArgumentException.class, () -> accrualUseCase.accrue(cmd));
        verify(accrualRepo, never()).save(any());
        verify(walletRepo, never()).save(any());
    }

    //사용자별 적립 그낭 금액 한도 초과
    @Test
    void accrue_throw_whenWalletBalanceExceeded() {
        long amount = 500L;

        when(pointPolicyService.minAccrualPerTxn()).thenReturn(1L);
        when(pointPolicyService.maxAccrualPerTxn()).thenReturn(1_000_000L);
        when(pointPolicyService.maxWalletBalanceFor(USER_ID)).thenReturn(10_000L);

        PointWallet wallet = mock(PointWallet.class);
        when(wallet.getTotalBalance()).thenReturn(9_800L);
        when(walletRepo.findById(USER_ID)).thenReturn(Optional.of(wallet));

        AccrualRequest request = AccrualRequestFactory.createWithDefalut(USER_ID, amount);
        CreateAccrual cmd = new CreateAccrual(request);

        assertThrows(IllegalArgumentException.class, () -> accrualUseCase.accrue(cmd));
        verify(accrualRepo, never()).save(any());
        verify(walletRepo, never()).save(any());
    }
}