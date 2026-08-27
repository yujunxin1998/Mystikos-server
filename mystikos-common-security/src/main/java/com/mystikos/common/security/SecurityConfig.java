package com.mystikos.common.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mystikos.common.security.persistence.SecurityWhitelistMapper;
import com.mystikos.common.security.persistence.SecurityWhitelistPO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * 只收紧了目前已经有真实鉴权需求的路径（认证本身、老板资料、文件存储、陪玩身份申请，
 * 以及所有归入 {@code /api/v1/manage/**} 前缀的后台管理接口——该前缀是后台管理接口的统一收口，
 * 新增管理接口只要挂在这个前缀下即自动纳入登录校验，方便后续做角色级权限收紧）。
 * 其他模块（Booking 等）的 C 端接口鉴权范围留给各自模块设计鉴权需求时收紧——
 * 这里默认放行，避免这次改动误伤还没设计鉴权的模块。
 *
 * <p>{@code /api/v1/auth/**} 下哪些子路径不需要登录（发验证码/注册/登录/刷新令牌/第三方登录）
 * 不再硬编码在这里，改成从 {@code security_whitelist_path} 表读（见 {@link SecurityWhitelistPO}），
 * 应用启动组装 {@link SecurityFilterChain} 时查一次库，之后运营改白名单不用发版，
 * 直接改表数据 + 重启即可。doc.html/swagger 这类框架自身的静态资源路径不算业务接口，仍然硬编码。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtTokenService jwtTokenService;
    private final SecurityWhitelistMapper securityWhitelistMapper;

    public SecurityConfig(JwtTokenService jwtTokenService, SecurityWhitelistMapper securityWhitelistMapper) {
        this.jwtTokenService = jwtTokenService;
        this.securityWhitelistMapper = securityWhitelistMapper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        List<SecurityWhitelistPO> whitelist = loadWhitelist();

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/doc.html", "/swagger-ui/**", "/swagger-resources/**",
                            "/v3/api-docs/**", "/webjars/**").permitAll();
                    auth.requestMatchers(HttpMethod.GET,
                            "/api/v1/auth/oauth/*/authorize",
                            "/api/v1/auth/oauth/*/callback").permitAll();
                    auth.requestMatchers(HttpMethod.POST, "/api/v1/auth/oauth/tickets").permitAll();
                    applyWhitelist(auth, whitelist);
                    auth.requestMatchers("/api/v1/auth/**").authenticated()
                            .requestMatchers("/api/v1/manage/**", "/api/v1/profile/**", "/api/v1/files/**",
                                    "/api/v1/companion-applications/**", "/api/v1/companion-showcase/**",
                                    "/api/v1/companions/**", "/api/v1/bookings/**", "/api/v1/my-orders/**")
                                    .authenticated()
                            .anyRequest().permitAll();
                })
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenService),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /** DB 不可用/表还没建（比如老环境没跑对应迁移）时按空白名单启动，只影响需要单独放行的接口，不影响服务整体起停。 */
    private List<SecurityWhitelistPO> loadWhitelist() {
        try {
            return securityWhitelistMapper.selectList(
                    new LambdaQueryWrapper<SecurityWhitelistPO>().eq(SecurityWhitelistPO::getEnabled, true));
        } catch (Exception e) {
            log.error("加载 Spring Security 白名单路径失败，本次启动按空白名单处理，"
                    + "security_whitelist_path 表里配置的公开接口本次会被当成需要登录", e);
            return List.of();
        }
    }

    private void applyWhitelist(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth,
            List<SecurityWhitelistPO> whitelist) {
        List<String> anyMethodPatterns = new ArrayList<>();
        for (SecurityWhitelistPO entry : whitelist) {
            String method = entry.getHttpMethod();
            if (method == null || method.isBlank()) {
                anyMethodPatterns.add(entry.getPathPattern());
                continue;
            }
            try {
                auth.requestMatchers(HttpMethod.valueOf(method.toUpperCase()), entry.getPathPattern()).permitAll();
            } catch (IllegalArgumentException e) {
                log.warn("忽略非法的白名单 HTTP 方法配置：pathPattern={}, httpMethod={}",
                        entry.getPathPattern(), method);
            }
        }
        if (!anyMethodPatterns.isEmpty()) {
            auth.requestMatchers(anyMethodPatterns.toArray(new String[0])).permitAll();
        }
    }
}
