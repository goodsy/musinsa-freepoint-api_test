
package com.musinsa.freepoint.application.port.in;

import com.musinsa.freepoint.adapters.in.web.dto.CancelUseRequest;
import com.musinsa.freepoint.adapters.in.web.dto.UseRequest;

public class UsageCommandPort {
    public record UsagePoint(UseRequest request) {}
    public record CancelUsagePoint(CancelUseRequest request) {}
}
