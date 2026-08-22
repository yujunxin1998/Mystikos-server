package com.mystikos.common.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 从 Authorization: Bearer 头解析自签发 JWT，校验通过就填充 SecurityContext。
 * 校验失败（缺失/过期/篡改）不在这里报错，直接放行给后续的授权检查，
 * 由 Spring Security 按路由规则决定要不要拒绝（未认证访问受保护路径会被拦成 401/403）。
 *
 * <p>这里的日志是排障关键——Spring Security 在过滤器链里直接拒绝请求不会经过
 * GlobalExceptionHandler，默认也不会有任何日志，出问题时是黑箱。DEBUG 级别，
 * 打的是头是否存在/格式对不对/校验失败原因，不打完整 token 明文。
 */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        String uri = request.getMethod() + " " + request.getRequestURI();

        if (header == null) {
            log.debug("{}：没有 Authorization 头", uri);
        } else if (!header.startsWith("Bearer ")) {
            log.debug("{}：Authorization 头格式不对，期望 \"Bearer <token>\"，实际前 20 字符 = {}",
                    uri, header.substring(0, Math.min(20, header.length())));
        } else {
            try {
                JwtTokenService.DecodedToken decoded = jwtTokenService.parse(header.substring(7));
                Collection<GrantedAuthority> authorities = decoded.roles().stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
                var authentication = new UsernamePasswordAuthenticationToken(
                        decoded.subject(), null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("{}：JWT 校验通过，subject={}, roles={}", uri, decoded.subject(), decoded.roles());
            } catch (JwtException e) {
                log.debug("{}：JWT 校验失败：{}", uri, e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }
}
