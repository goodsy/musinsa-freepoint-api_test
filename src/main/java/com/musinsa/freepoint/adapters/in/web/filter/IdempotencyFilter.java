
package com.musinsa.freepoint.adapters.in.web.filter;

import com.musinsa.freepoint.adapters.in.web.ApiHeaderConstants;
import com.musinsa.freepoint.application.port.in.IdempotencyPort;
import com.musinsa.freepoint.application.service.IdempotencyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class IdempotencyFilter extends OncePerRequestFilter {
    private final IdempotencyService idempotencyService;
    private static final String HEADER = "Idempotency-Key";

    public IdempotencyFilter(IdempotencyService idempotencyService) {
        this.idempotencyService = idempotencyService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String key = request.getHeader(HEADER);
        if (key == null || key.isBlank() || !isIdempotentMethod(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper req = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper res = new ContentCachingResponseWrapper(response);

        // Compute request hash
        //String payload = new String(req.getContentAsByteArray(), StandardCharsets.UTF_8);
        //String requestHash = RequestUtils.md5(request.getMethod() + "|" + request.getRequestURI() + "|" + payload);

        String idemKey = request.getHeader(ApiHeaderConstants.IDEMPOTENCY_KEY);

        Optional<IdempotencyPort.CachedResponse> cached = idempotencyService.preCheck(idemKey);

        if (cached.isPresent()) {

            IdempotencyPort.CachedResponse c = cached.get();
            res.setStatus(c.statusCode());
            res.setHeader(HttpHeaders.CONTENT_TYPE,
                    c.headers() != null ? c.headers() : MediaType.APPLICATION_JSON_VALUE);
            StreamUtils.copy(c.body().getBytes(StandardCharsets.UTF_8), res.getResponse().getOutputStream());
            res.copyBodyToResponse();
            return;
        }

        // 캐쉬가 없을 경우 실제 서비스 로직 수행
        filterChain.doFilter(req, res);

        // Only cache success (2xx) and 409 (conflict) by design; adjust as needed
        int status = res.getStatus();
        if ((status >= 200 && status < 300) || status == 409) {
            String respBody = new String(res.getContentAsByteArray(), StandardCharsets.UTF_8);
            String respContentType = res.getHeader(HttpHeaders.CONTENT_TYPE);
            idempotencyService.saveOrUpdate(idemKey, status, respBody);
        }

        res.copyBodyToResponse();
    }

    private boolean isIdempotentMethod(String method) {
        // Typically we guard POST; PUT/PATCH can be retried if properly designed. Here apply to POST only.
        return "POST".equalsIgnoreCase(method);
    }

    /*private static final Set<String> WRITE_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");
    private final IdempotencyStore store;
    private final String headerName;
    private final boolean requiredOnWrite;
    private final IdempotencyService service;

    public IdempotencyFilter(IdempotencyStore store, String headerName, boolean requiredOnWrite, IdempotencyService service) {
        this.store = store;
        this.headerName = headerName;
        this.requiredOnWrite = requiredOnWrite;
        this.service = service;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !WRITE_METHODS.contains(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String key = req.getHeader(headerName);
        if ((key == null || key.isBlank()) && !requiredOnWrite) {
            chain.doFilter(req, res);
            return;
        }
        if (key == null || key.isBlank()) {
            ApiResponse.filterSendError(res, HttpServletResponse.SC_BAD_REQUEST, ApiErrorCode.HEADER_MISSING_IDEMPOTENCY_KEY);
            //res.sendError(422, "Missing Idempotency-Key");
            return;
        }

        ContentCachingRequestWrapper request = new ContentCachingRequestWrapper(req);
        ContentCachingResponseWrapper response = new ContentCachingResponseWrapper(res);

        final String uri = request.getRequestURI();
        final String method = request.getMethod();

        var found = store.find(key, uri);
        if (found != null) {
            res.setStatus(found.status());
            res.getOutputStream().write(found.body());
            return;
        }

        try {
            chain.doFilter(request, response);
        } finally {
            byte[] body = response.getContentAsByteArray();
            int status = response.getStatus();
            String requestHash = hash(method, uri, readRequestBodySafe(request));
            store.save(key, uri, new IdempotencyRecord(status, body, requestHash), Duration.ofDays(7));
            response.copyBodyToResponse();
        }
    }

    private static String readRequestBodySafe(ContentCachingRequestWrapper req) {
        byte[] content = req.getContentAsByteArray();
        return content.length == 0 ? "" : new String(content, StandardCharsets.UTF_8);
    }

    private static String hash(String method, String uri, String body) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(method.getBytes(StandardCharsets.UTF_8));
            md.update((byte)':');
            md.update(uri.getBytes(StandardCharsets.UTF_8));
            md.update((byte)':');
            md.update(body.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : md.digest()) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return "NA";
        }
    }

    // === record & store ===
    public record IdempotencyRecord(int status, byte[] body, String requestHash) {}

    public interface IdempotencyStore {
        IdempotencyRecord find(String key, String uri);
        void save(String key, String uri, IdempotencyRecord record, Duration ttl);
    }*/
}
