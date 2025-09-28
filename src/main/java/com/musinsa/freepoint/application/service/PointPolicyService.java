package com.musinsa.freepoint.application.service;

import com.musinsa.freepoint.adapters.out.persistence.PointPolicyRuleRepository;
import com.musinsa.freepoint.config.PointPolicyConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PointPolicyService {

    private final PointPolicyRuleRepository policyRepository;
    private final PointPolicyConfig config;

    private static final String MAX_WALLET_BALANCE = "wallet.maxBalance";
    private static final String MIN_ACCRUAL_PER_TXN = "accrual.minPerTxn";
    private static final String MAX_ACCRUAL_PER_TXN  = "accrual.maxPerTxn";
    private static final String MAX_EXPIRY_DAYS = "expiry.maxDays";


    @Cacheable(cacheNames = "policy", key = "'u:' + (#scopeId?:'')")
    public Map<String, String> policyMap(String scopeId) {
        return policyRepository.findAllByScopeId(scopeId).stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> (String) arr[1]
                ));
    }

    private String getPolicyValue(String key, String scopeId) {
        Map<String, String> map = policyMap(scopeId);
        return map.get(key);
    }

    public long maxWalletBalanceFor(String scopeId) {
        String v = getPolicyValue(MAX_WALLET_BALANCE, scopeId);
        return toLong(v, config.getMaxWalletBalance());
    }

    public long minAccrualPerTxn() {
        String v = getPolicyValue(MIN_ACCRUAL_PER_TXN, null);
        return toLong(v, config.getMinAccrualPerTxn());
    }

    public long maxAccrualPerTxn() {
        String v = getPolicyValue(MAX_ACCRUAL_PER_TXN, null);
        return toLong(v, config.getMaxAccrualPerTxn());
    }

    public int defaultExpiryDays() {
        String v = getPolicyValue(MAX_EXPIRY_DAYS, null);
        return toInt(v, config.getDefaultExpiryDays());
    }

    public int maxExpiryDays() {
        return config.getMaxExpiryDays();
    }
    /*
    @Cacheable(cacheNames = "policy", key = "'k:' + #policyKey + ':u:' + (#scopeId?:''))")
    public String bestValue(String policyKey, String scopeId) {
        return policyRuleRepo.findBestValue(policyKey, scopeId).orElse(null);
    }

    public long maxWalletBalanceFor(String scopeId) {
        String v = bestValue(MAX_WALLET_BALANCE, scopeId);
        return toLong(v, config.getMaxWalletBalance());
    }

    public long minAccrualPerTxn() {
        String v = bestValue(MIN_ACCRUAL_PER_TXN, null);
        return toLong(v, config.getMinAccrualPerTxn());
    }

    public long maxAccrualPerTxn() {
        String v = bestValue(MAX_ACCRUAL_PER_TXN, null);
        return toLong(v, config.getMaxAccrualPerTxn());
    }

    public int defaultExpiryDays() {
        String v = bestValue(MAX_EXPIRY_DAYS, null);
        return toInt(v, config.getDefaultExpiryDays());
    }

    public int maxExpiryDays() {
        return config.getMaxExpiryDays();
    }

     */

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
}
