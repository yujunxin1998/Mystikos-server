package com.mystikos.systemoperation.infrastructure.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

/**
 * 只把 {@code /api/v1/manage/**} 的请求体缓存下来，供之后 {@code OperationLogInterceptor}
 * 在 {@code afterCompletion} 里再读一次（Controller 用 {@code @RequestBody} 读过一次输入流后，
 * 不包一层缓存就读不到第二次）。不改响应，不影响正常业务处理，注册范围见 WebMvcConfig。
 */
public class OperationLogRequestWrappingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        chain.doFilter(new ContentCachingRequestWrapper(request), response);
    }
}
