
package com.musinsa.freepoint.adapters.out.persistence;
import com.musinsa.freepoint.domain.wallet.PointWallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointWalletRepository extends JpaRepository<PointWallet, String> {
    Optional<PointWallet> findByUserId(String userId);
}
