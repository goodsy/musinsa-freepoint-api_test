
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
    private long minAccrualPerTxn = 1;
    private long maxAccrualPerTxn = 100_000L;
    private long maxWalletBalance = 10_000_000L;

    private int defaultExpiryDays = 365;
    private int minExpiryDays = 1;

}
