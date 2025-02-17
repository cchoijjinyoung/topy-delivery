package com.fourseason.delivery.global.auth;

import com.fourseason.delivery.domain.member.entity.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

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

    public String createAccessToken(String username, Role role) {
        return generateToken(username, role, Duration.ofMinutes(accessExpiration));
    }

    public String createRefreshToken(String username) {
        return generateToken(username, null, Duration.ofMinutes(refreshExpiration));
    }

    // 토큰 생성
    private String generateToken(String username, Role role, Duration expiration) {
        // aws 의 리전이 어디에 생길지 모르니 타임 존을 명확하게 ..
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuer(issuer)
                .issuedAt(Date.from(now.toInstant()))
                .expiration(Date.from(now.plusMinutes(expiration.toMillis()).toInstant()))
                .signWith(secretKey)
                .compact();
    }

    // 토큰 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException | SignatureException e) {
            //TODO: customException 으로 변경
            log.error("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.");
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token, 만료된 JWT token 입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.");
        } catch (IllegalArgumentException e) {
            log.error("JWT claims is empty, 잘못된 JWT 토큰 입니다.");
        }
        return false;
    }

    // 토큰에서 사용자 정보 가져오기
    public Claims getMemberInfoFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
