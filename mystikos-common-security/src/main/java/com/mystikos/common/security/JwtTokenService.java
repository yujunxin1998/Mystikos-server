package com.mystikos.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 自签发 JWT 的签发与校验。只负责 access token（无状态、短期有效）；
 * refresh token 是持久化的不透明串，不走这里，见 mystikos-identity 的 RefreshToken。
 */
@Component
public class JwtTokenService {

    private final SecretKey key;
    private final Duration accessTokenTtl;

    public JwtTokenService(
            @Value("${mystikos.jwt.secret}") String secret,
            @Value("${mystikos.jwt.access-token-ttl-minutes:120}") long accessTokenTtlMinutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenTtl = Duration.ofMinutes(accessTokenTtlMinutes);
    }

    public String generateAccessToken(String subject, Set<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(subject)
                .claim("roles", roles)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(accessTokenTtl)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /** 校验失败（签名不对/已过期/格式错误）统一抛 {@link JwtException}，调用方不用关心具体子类。 */
    public DecodedToken parse(String token) throws JwtException {
        Jws<Claims> jws = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
        Claims claims = jws.getBody();
        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);
        Set<String> roleSet = roles == null ? Set.of() : roles.stream().collect(Collectors.toSet());
        return new DecodedToken(claims.getSubject(), roleSet);
    }

    public record DecodedToken(String subject, Set<String> roles) {
    }
}
