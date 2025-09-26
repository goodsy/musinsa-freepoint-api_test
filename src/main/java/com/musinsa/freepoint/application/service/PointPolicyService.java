package com.musinsa.freepoint.application.service;

import com.musinsa.freepoint.adapters.out.persistence.PointPolicyRuleRepository;
import com.musinsa.freepoint.config.PointPolicyConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointPolicyService {

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
    //private static final String K_EXP_MIN = "expiry.minDays";
    //private static final String K_EXP_MAX = "expiry.maxDays";
    private static final String K_EXP_DEFAULT = "expiry.defaultDays";

    @Cacheable(cacheNames = "policy", key = "'k:' + #policyKey + ':u:' + (#scopeId?:''))")
    public String bestValue(String policyKey, String scopeId) {
        return repository.findBestValue(policyKey, scopeId).orElse(null);
    }

    public long maxWalletBalanceFor(String scopeId) {
        String v = bestValue(K_WALLET_MAX, scopeId);
        return toLong(v, config.getMaxWalletBalance());
    }

    public long minAccrualPerTxn() {
        String v = bestValue(K_MIN_PER_TXN, null);
        return toLong(v, config.getMinAccrualPerTxn());
    }

    public long maxAccrualPerTxn() {
        String v = bestValue(K_MAX_PER_TXN, null);
        return toLong(v, config.getMaxAccrualPerTxn());
    }

    public int defaultExpiryDays() {
        String v = bestValue(K_EXP_DEFAULT, null);
        return toInt(v, config.getDefaultExpiryDays());
    }

    /*public int minExpiryDays(String scopeId) {
        String v = bestValue(K_EXP_MIN, scopeId);
        return toInt(v, config.getMinExpiryDays());
    }

    public int maxExpiryDays(String scopeId) {
        String v = bestValue(K_EXP_MAX, scopeId);
        return toInt(v, config.getMaxExpiryDays());
    }*/
}
