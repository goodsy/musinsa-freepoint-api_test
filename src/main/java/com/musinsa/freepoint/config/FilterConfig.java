
package com.musinsa.freepoint.config;

import com.musinsa.freepoint.adapters.in.web.filter.ApiLoggingFilter;
import com.musinsa.freepoint.adapters.in.web.filter.IdempotencyFilter;
import com.musinsa.freepoint.adapters.out.logsink.ApiLogSink;
import com.musinsa.freepoint.adapters.out.logsink.AsyncConsoleLogSink;
import com.musinsa.freepoint.adapters.out.persistence.ApiLogRepository;
import com.musinsa.freepoint.application.service.IdempotencyService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {


   @Bean
    public FilterRegistrationBean<IdempotencyFilter> idempotencyFilter(IdempotencyService service) {
        FilterRegistrationBean<IdempotencyFilter> bean = new FilterRegistrationBean<>();
        //bean.setFilter(new IdempotencyFilter(new InMemoryIdempotencyStore(), ApiHeaderConstants.IDEMPOTENCY_KEY, true, service));
        bean.setFilter(new IdempotencyFilter(service));
        bean.setOrder(1); // run first
        bean.addUrlPatterns("/*");
        return bean;
    }

    @Bean
    public FilterRegistrationBean<ApiLoggingFilter> apiLoggingFilter(ApiLogRepository repo, ApiLogSink sink) {
        FilterRegistrationBean<ApiLoggingFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new ApiLoggingFilter(repo, sink));
        bean.setOrder(2); // run after idempotency so cached responses are logged too
        bean.addUrlPatterns("/*");
        return bean;
    }



/*
    @Bean
    public IdempotencyFilter idempotencyFilterBean() {
        return new IdempotencyFilter(new InMemoryIdempotencyStore(), ApiHeaderConstants.IDEMPOTENCY_KEY, true);
    }

    @Bean
    public FilterRegistrationBean<IdempotencyFilter> idempotencyFilter(IdempotencyFilter f) {
        FilterRegistrationBean<IdempotencyFilter> bean = new FilterRegistrationBean<>(f);
        bean.setOrder(1);
        bean.addUrlPatterns("/api/*");
        return bean;
    }

    @Bean
    public ApiLogSink apiLogSink() { return new AsyncConsoleLogSink(); }

    @Bean
    public ApiLoggingFilter apiLoggingFilterBean(ApiLogSink sink) {
        return new ApiLoggingFilter(sink);
    }

    @Bean
    public FilterRegistrationBean<ApiLoggingFilter> apiLoggingFilter(ApiLoggingFilter f) {
        FilterRegistrationBean<ApiLoggingFilter> bean = new FilterRegistrationBean<>(f);
        bean.setOrder(2);
        bean.addUrlPatterns("/api/*");
        return bean;
    }*/
}
