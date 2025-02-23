package com.fourseason.delivery.global.auth;

import com.fourseason.delivery.domain.member.entity.Role;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JwtUtilTest {

    public static final Logger log = LoggerFactory.getLogger(JwtUtilTest.class);

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {

        String secret = "secret1234567890123456789012345678901234567890";
        String issuer = "project1";
        Long accessExpiration = 30 * 60L;
        Long refreshExpiration = 7L;
        jwtUtil = new JwtUtil(secret, issuer, accessExpiration, refreshExpiration);
    }

    @Test
    @DisplayName("토큰 생성 확인")
    void createToken() {
        String token = jwtUtil.createAccessToken("testUser", Role.CUSTOMER, 1L);

        log.info(token);
        assertNotNull(token);
    }

    @Test
    @DisplayName("access 토큰 값 검증")
    void testGetMemberInfoFromAccessToken() {
        String token = jwtUtil.createAccessToken("testUser", Role.CUSTOMER, 3L);

        Claims claims = jwtUtil.validateToken(token);

        log.info("access 클레임 확인: {}", claims.toString());

        assertNotNull(claims);
        assertEquals("testUser", claims.getSubject());
        assertEquals(3L, claims.get("id", Long.class));
        assertEquals(Role.CUSTOMER.toString(), claims.get("role", String.class));
    }

    @Test
    @DisplayName("refresh 토큰 값 검증")
    void testGetMemberInfoFromRefreshToken() {
        String token = jwtUtil.createRefreshToken(5L);

        Claims claims = jwtUtil.validateToken(token);

        log.info("refresh 클레임 확인: {}", claims.toString());

        assertNotNull(claims);
        assertEquals(5L, claims.get("id", Long.class));
    }
}