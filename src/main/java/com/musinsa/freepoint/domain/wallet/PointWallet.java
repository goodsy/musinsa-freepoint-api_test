
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
    private long totalBalance;
    private long manualBalance;
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
}
