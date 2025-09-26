
package com.musinsa.freepoint.common.idempotency;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Component
public class IdempotencyFilter extends OncePerRequestFilter {
    private final IdempotencyRepository repo;
    public IdempotencyFilter(IdempotencyRepository repo) { this.repo = repo; }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equalsIgnoreCase(request.getMethod());
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String key = request.getHeader("Idempotency-Key");
        if (key == null || key.isBlank()) { filterChain.doFilter(request, response); return; }
        var existing = repo.findById(key);
        if (existing.isPresent()) {
            var e = existing.get();
            response.setStatus(e.getStatus() == null ? 200 : e.getStatus());
            response.setContentType("application/json");
            response.getWriter().write(e.getResponseBody() == null ? "{}" : e.getResponseBody());
            return;
        }
        ContentCachingResponseWrapper wrap = new ContentCachingResponseWrapper(response);
        filterChain.doFilter(request, wrap);
        String body = new String(wrap.getContentAsByteArray(), StandardCharsets.UTF_8);
        String hash = hash(request.getMethod() + "|" + request.getRequestURI() + "|" + body);
        repo.save(IdempotencyEntity.of(key, hash, request.getMethod(), request.getRequestURI(), body, wrap.getStatus()));
        wrap.copyBodyToResponse();
    }
    private static String hash(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(md.digest(s.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) { return ""; }
    }
}
