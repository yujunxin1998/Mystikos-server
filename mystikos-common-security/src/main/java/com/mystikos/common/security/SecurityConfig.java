package com.mystikos.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 只收紧了目前已经有真实鉴权需求的路径（Identity 的角色管理/封禁/资料、认证本身）。
 * 其他模块（Booking 等）的接口鉴权范围留给各自模块设计鉴权需求时收紧——
 * 这里默认放行，避免这次改动误伤还没设计鉴权的模块。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtTokenService jwtTokenService;

    public SecurityConfig(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/doc.html", "/swagger-ui/**", "/swagger-resources/**",
                                "/v3/api-docs/**", "/webjars/**").permitAll()
                        .requestMatchers("/api/v1/auth/verification-codes", "/api/v1/auth/register",
                                "/api/v1/auth/login", "/api/v1/auth/refresh-token",
                                "/api/v1/auth/oauth/*/login").permitAll()
                        .requestMatchers("/api/v1/auth/**").authenticated()
                        .requestMatchers("/api/v1/users/*/roles/**", "/api/v1/users/*/ban",
                                "/api/v1/profile/**", "/api/v1/files/**").authenticated()
                        .anyRequest().permitAll())
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenService),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
