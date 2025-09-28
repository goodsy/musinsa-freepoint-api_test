
package com.musinsa.freepoint.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Setter
@Getter
@ConfigurationProperties(prefix = "point.policy")
public class PointPolicyConfig {
    private long minAccrualPerTxn;
    private long maxAccrualPerTxn;
    private long maxWalletBalance;

    private int defaultExpiryDays;
    private int maxExpiryDays;

}
