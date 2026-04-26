package com.triple.travel.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

/**
 * HS256 기반 JWT 토큰 생성/검증.
 * subject = userId (Long → String)
 * email은 클레임으로 함께 실어 보냄.
 */
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final Duration accessTokenTtl;

    public JwtTokenProvider(
        @Value("${app.jwt.secret}") String secret,
        @Value("${app.jwt.access-token-expiry-hours}") long expiryHours
    ) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                "app.jwt.secret must be at least 256 bits (32 bytes). Current: " + keyBytes.length);
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenTtl = Duration.ofHours(expiryHours);
    }

    public String issueAccessToken(Long userId, String email) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + accessTokenTtl.toMillis());
        return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim("email", email)
            .issuedAt(now)
            .expiration(exp)
            .signWith(key)
            .compact();
    }

    public Long parseUserId(String token) {
        Claims claims = parseClaims(token);
        return Long.valueOf(claims.getSubject());
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public long getAccessTokenTtlSeconds() {
        return accessTokenTtl.getSeconds();
    }
}
