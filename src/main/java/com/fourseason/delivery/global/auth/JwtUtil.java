package com.fourseason.delivery.global.auth;

import com.fourseason.delivery.domain.member.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;

/**
 * Jwt 생성 및 검증 담당
 */
@Slf4j(topic = "JwtUtil")
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final String issuer;
    private final Long accessExpiration;
    private final Long refreshExpiration;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.issuer}") String issuer,
            @Value("${jwt.access-expiration}") Long accessExpiration,
            @Value("${jwt.refresh-expiration}") Long refreshExpiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.issuer = issuer;
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    public String createAccessToken(String username, Role role, Long id) {
        Map<String, Object> claims = Map.of(
                "role", role,
                "id", id);

        return generateToken(Duration.ofMinutes(accessExpiration), username, claims);
    }

    public String createRefreshToken(Long id) {

        return generateToken(Duration.ofDays(refreshExpiration), null, Map.of("id", id));
    }

    // 토큰 생성
    private String generateToken(Duration expiration, String username, Map<String, Object> claims) {
        // aws 의 리전이 어디에 생길지 모르니 타임 존을 명확하게 ...
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        return Jwts.builder()
                .subject(username)
                .claims(claims)
                .issuer(issuer)
                .issuedAt(Date.from(now.toInstant()))
                .expiration(Date.from(now.plus(expiration).toInstant()))
                .signWith(secretKey)
                .compact();
    }

    // 토큰에서 사용자 정보 가져오기
    public Claims validateToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
