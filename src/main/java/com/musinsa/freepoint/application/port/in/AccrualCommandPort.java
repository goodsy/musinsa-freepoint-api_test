
package com.musinsa.freepoint.application.port.in;

import com.musinsa.freepoint.adapters.in.web.dto.AccrualRequest;
import com.musinsa.freepoint.adapters.in.web.dto.CancelAccrualRequest;

public class AccrualCommandPort {
    public record CreateAccrual(AccrualRequest request) {}
    public record CancelAccrual(CancelAccrualRequest request) {}

}
