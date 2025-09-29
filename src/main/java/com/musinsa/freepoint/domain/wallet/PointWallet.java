
package com.musinsa.freepoint.domain.wallet;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "point_wallet")
public class PointWallet {
    @Id
    @Column(name = "user_id", length = 64)
    private String userId;

    @Column(name = "total_balance")
    private long totalBalance;

    @Column(name = "manual_balance")
    private long manualBalance;

    @Column(name = "total_used")
    private long totalUsed;

    @Column(name = "total_canceled")
    private long totalCanceled; // [추가]

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public static PointWallet create(String userId) {
        PointWallet wallet = new PointWallet();
        wallet.userId = userId;
        wallet.totalBalance = 0;
        wallet.manualBalance = 0;
        wallet.updatedAt = LocalDateTime.now();
        return wallet;

    }
    public void increase(long amount, boolean manual) {
        this.totalBalance += amount;
        if (manual) this.manualBalance += amount;
        this.updatedAt = LocalDateTime.now();
    }

    public void decrease(long amount) {
        this.totalBalance -= amount;
        long dec = Math.min(this.manualBalance, amount);
        this.manualBalance -= dec;
        this.updatedAt = LocalDateTime.now();
    }

    public void use(long amount, long usedFromManual) {
        this.totalBalance -= amount;
        this.totalUsed += amount;
        if (usedFromManual > 0) {
            this.manualBalance -= usedFromManual;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void restore(long amount, long restoreToManual) {
        this.totalBalance += amount;
        this.totalCanceled += amount;
        if (restoreToManual > 0) {
            this.manualBalance += restoreToManual;
        }
        this.updatedAt = LocalDateTime.now();
    }

}
