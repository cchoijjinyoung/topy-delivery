package com.fourseason.delivery.global.auth;

import com.fourseason.delivery.domain.member.entity.Role;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    public static final Logger log = LoggerFactory.getLogger(JwtUtilTest.class);

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        String secret = "7Iqk7YyM66W07YOA7L2U65Sp7YG065+9U3ByaW5n6rCV7J2Y7Yqc7YSw7LWc7JuQ67mI7J6F64uI64ukLg==";
        String issuer = "s1jin-delivery";
        Long accessExpiration = 30 * 60L;
        Long refreshExpiration = 1000 * 60 * 60L;
        jwtUtil = new JwtUtil(secret, issuer, accessExpiration, refreshExpiration);
    }

    @Test
    @DisplayName("토큰 생성 확인")
    void createToken() {
        String token = jwtUtil.createAccessToken("testUser", Role.CUSTOMER);

        log.info(token);
        assertNotNull(token);
        assertTrue(token.startsWith("Bearer "));
    }

    @Test
    @DisplayName("생성된 토큰 검증")
    void validateToken() {
        String token = jwtUtil.createAccessToken("testUser", Role.CUSTOMER);
        token = token.replace("Bearer ", "");

        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    @DisplayName("access 토큰 값 검증")
    void testGetMemberInfoFromAccessToken() {
        String token = jwtUtil.createAccessToken("testUser", Role.CUSTOMER);

        Claims claims = jwtUtil.getMemberInfoFromToken(token);

        log.info("access 클레임 확인: {}", claims.toString());

        assertNotNull(claims);
        assertEquals("testUser", claims.getSubject());
        assertEquals("CUSTOMER", claims.get("role").toString());
    }

    @Test
    @DisplayName("refresh 토큰 값 검증")
    void testGetMemberInfoFromRefreshToken() {
        String token = jwtUtil.createRefreshToken("testUser");

        Claims claims = jwtUtil.getMemberInfoFromToken(token);

        log.info("refresh 클레임 확인: {}", claims.toString());

        assertNotNull(claims);
        assertEquals("testUser", claims.getSubject());
    }
}