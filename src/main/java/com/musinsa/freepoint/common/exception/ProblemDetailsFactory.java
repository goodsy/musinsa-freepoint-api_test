package com.musinsa.freepoint.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * RFC9457 ProblemDetail 간편 팩토리.
 * 차별화 포인트: 제목/세부/코드/타임스탬프/추가속성 일괄 세팅.
 */
public final class ProblemDetailsFactory {

    private ProblemDetailsFactory() {}

    public static ProblemDetail of(HttpStatus status, String title, ErrorCode errorCode, String detail, Map<String, Object> props) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail != null ? detail : title);
        pd.setTitle(title);
        pd.setType(URI.create("about:blank"));
        pd.setProperty("errorCode", errorCode != null ? errorCode.code() : null);
        pd.setProperty("timestamp", OffsetDateTime.now().toString());
        if (props != null) props.forEach(pd::setProperty);
        return pd;
    }
}
