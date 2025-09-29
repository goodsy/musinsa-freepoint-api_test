package com.musinsa.freepoint.domain.accural;

import com.musinsa.freepoint.adapters.in.web.dto.AccrualRequest;
import com.musinsa.freepoint.adapters.in.web.exception.ApiErrorCode;
import com.musinsa.freepoint.domain.DomainException;
import com.musinsa.freepoint.domain.accrual.AccrualStatus;
import com.musinsa.freepoint.domain.accrual.PointAccrual;
import org.assertj.core.api.AbstractDateAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PointAccrualTest {
    @Test
    @DisplayName("Create accrual - success")
    void createAccrual_success() {
        AccrualRequest request = mock(AccrualRequest.class);
        when(request.userId()).thenReturn("user1");
        when(request.amount()).thenReturn(1000L);
        when(request.manual()).thenReturn(false);
        when(request.sourceType()).thenReturn("ORDER");
        when(request.sourceId()).thenReturn("order-123");
        when(request.expiryDays()).thenReturn(30);

        PointAccrual accrual = PointAccrual.create(request);

        assertThat(accrual.getUserId()).isEqualTo("user1");
        assertThat(accrual.getAmount()).isEqualTo(1000L);
        assertThat(accrual.getRemainAmount()).isEqualTo(1000L);
        assertThat(accrual.getStatus()).isEqualTo(AccrualStatus.ACTIVE.name());
        assertThat(accrual.getExpireAt()).isAfter(LocalDateTime.now());
    }

    private <SELF extends AbstractDateAssert<SELF>> AbstractDateAssert<SELF> assertThat(String userId) {
        return null;
    }

    @Test
    @DisplayName("Use accrual - deduct success")
    void useAccrual_deductSuccess() {
        PointAccrual accrual = PointAccrual.builder()
                .pointKey("key")
                .userId("user1")
                .amount(1000L)
                .remainAmount(1000L)
                .manual(false)
                .sourceType("ORDER")
                .sourceId("order-1")
                .expireAt(LocalDateTime.now().plusDays(10))
                .status(AccrualStatus.ACTIVE.name())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        accrual.use(500L);

        assertThat(accrual.getRemainAmount()).isEqualTo(500L);
    }

    @Test
    @DisplayName("Use accrual - insufficient balance exception")
    void useAccrual_insufficientBalanceException() {
        PointAccrual accrual = PointAccrual.builder()
                .pointKey("key")
                .userId("user1")
                .amount(1000L)
                .remainAmount(100L)
                .manual(false)
                .sourceType("ORDER")
                .sourceId("order-1")
                .expireAt(LocalDateTime.now().plusDays(10))
                .status(AccrualStatus.ACTIVE.name())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        assertThatThrownBy(() -> accrual.use(200L))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", ApiErrorCode.INSUFFICIENT_BALANCE);
    }

    @Test
    @DisplayName("Cancel accrual - success")
    void cancelAccrual_success() {
        PointAccrual accrual = PointAccrual.builder()
                .pointKey("key")
                .userId("user1")
                .amount(1000L)
                .remainAmount(1000L)
                .manual(false)
                .sourceType("ORDER")
                .sourceId("order-1")
                .expireAt(LocalDateTime.now().plusDays(10))
                .status(AccrualStatus.ACTIVE.name())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        accrual.cancel();

        assertThat(accrual.getStatus()).isEqualTo(AccrualStatus.CANCELED.name());
    }

    @Test
    @DisplayName("Cancel accrual - used point cancellation not allowed exception")
    void cancelAccrual_usedPointCancellationNotAllowedException() {
        PointAccrual accrual = PointAccrual.builder()
                .pointKey("key")
                .userId("user1")
                .amount(1000L)
                .remainAmount(500L)
                .manual(false)
                .sourceType("ORDER")
                .sourceId("order-1")
                .expireAt(LocalDateTime.now().plusDays(10))
                .status(AccrualStatus.ACTIVE.name())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        assertThatThrownBy(accrual::cancel)
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", ApiErrorCode.USED_POINT_CANCELLATION_NOT_ALLOWED);
    }

    @Test
    @DisplayName("Restore accrual - success")
    void restoreAccrual_success() {
        PointAccrual accrual = PointAccrual.builder()
                .pointKey("key")
                .userId("user1")
                .amount(1000L)
                .remainAmount(500L)
                .manual(false)
                .sourceType("ORDER")
                .sourceId("order-1")
                .expireAt(LocalDateTime.now().plusDays(10))
                .status(AccrualStatus.ACTIVE.name())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        accrual.restore(200L);

        assertThat(accrual.getRemainAmount()).isEqualTo(700L);
    }

    @Test
    @DisplayName("Check accrual expired - true")
    void checkAccrualExpired_true() {
        PointAccrual accrual = PointAccrual.builder()
                .pointKey("key")
                .userId("user1")
                .amount(1000L)
                .remainAmount(1000L)
                .manual(false)
                .sourceType("ORDER")
                .sourceId("order-1")
                .expireAt(LocalDateTime.now().minusDays(1))
                .status(AccrualStatus.ACTIVE.name())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        assertThat(accrual.isExpired()).isTrue();
    }
}
