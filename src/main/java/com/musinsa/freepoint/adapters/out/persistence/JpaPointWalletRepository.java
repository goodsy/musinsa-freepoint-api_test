
package com.musinsa.freepoint.adapters.out.persistence;
import com.musinsa.freepoint.domain.wallet.PointWallet;
import org.springframework.data.jpa.repository.JpaRepository;
public interface JpaPointWalletRepository extends JpaRepository<PointWallet, String> { }
