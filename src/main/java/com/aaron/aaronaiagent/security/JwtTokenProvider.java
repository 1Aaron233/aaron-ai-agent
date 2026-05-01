package com.aaron.aaronaiagent.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long expireSeconds;

    public JwtTokenProvider(
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.expire-seconds:7200}") long expireSeconds
    ) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expireSeconds = expireSeconds;
    }

    public String generateToken(SecurityUser user) {
        Instant now = Instant.now();
        Instant expireAt = now.plusSeconds(expireSeconds);
        return Jwts.builder()
                .subject(String.valueOf(user.getUserId()))
                .claim("username", user.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expireAt))
                .signWith(secretKey)
                .compact();
    }

    public Long parseUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.parseLong(claims.getSubject());
    }

    public long getExpireSeconds() {
        return expireSeconds;
    }
}
