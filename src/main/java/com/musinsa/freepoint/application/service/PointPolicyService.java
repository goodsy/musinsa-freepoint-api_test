package com.musinsa.freepoint.application.service;

import com.musinsa.freepoint.adapters.out.persistence.PointPolicyRuleRepository;
import com.musinsa.freepoint.config.PointPolicyConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PolicyService {
    private final PointPolicyRuleRepository repository;
    private final PointPolicyConfig config;

    private long toLong(String v, long fallback) {
        try {
            return (v != null) ? Long.parseLong(v) : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }

    private int toInt(String v, int fallback) {
        try {
            return (v != null) ? Integer.parseInt(v) : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }

    private static final String K_WALLET_MAX = "wallet.maxBalance";
    private static final String K_MIN_PER_TXN = "accrual.minPerTxn";
    private static final String K_MAX_PER_TXN = "accrual.maxPerTxn";
    private static final String K_EXP_MIN = "expiry.minDays";
    private static final String K_EXP_MAX = "expiry.maxDays";
    private static final String K_EXP_DEFAULT = "expiry.defaultDays";

    @Cacheable(cacheNames = "policy", key = "'k:' + #policyKey + ':u:' + (#userId?:''))")
    public String bestValue(String policyKey, String userId) {
        return repository.findBestValue(policyKey, userId).orElse(null);
    }

    public long maxWalletBalanceFor(String userId) {
        String v = bestValue(K_WALLET_MAX, userId);
        return toLong(v, config.getMaxWalletBalance());
    }

    public long minPerTxn() {
        String v = bestValue(K_MIN_PER_TXN, userId);
        return toLong(v, config.getMinAccrualPerTxn());
    }

    public long maxPerTxn(String userId) {
        String v = bestValue(K_MAX_PER_TXN, userId);
        return toLong(v, config.getMaxAccrualPerTxn());
    }

    public int minExpiryDays(String userId) {
        String v = bestValue(K_EXP_MIN, userId);
        return toInt(v, config.getMinExpiryDays());
    }

    public int maxExpiryDays(String userId) {
        String v = bestValue(K_EXP_MAX, userId);
        return toInt(v, config.getMaxExpiryDays());
    }

    public int defaultExpiryDays(String userId) {
        String v = bestValue(K_EXP_DEFAULT, userId);
        return toInt(v, config.getDefaultExpiryDays());
    }
}
