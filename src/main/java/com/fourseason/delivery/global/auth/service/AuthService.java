package com.fourseason.delivery.global.auth.service;

import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.member.repository.MemberRepository;
import com.fourseason.delivery.global.auth.JwtUtil;
import com.fourseason.delivery.global.auth.dto.TokenDto;
import com.fourseason.delivery.global.auth.dto.request.SignInRequestDto;
import com.fourseason.delivery.global.auth.dto.request.SignUpRequestDto;
import com.fourseason.delivery.global.exception.CustomException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.fourseason.delivery.domain.member.exception.MemberErrorCode.*;
import static com.fourseason.delivery.global.auth.exception.AuthErrorCode.ACCESS_TOKEN_NOT_AVAILABLE;
import static com.fourseason.delivery.global.auth.exception.AuthErrorCode.REFRESH_TOKEN_NOT_AVAILABLE;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public void signUp(SignUpRequestDto request) {
        // username 중복 불가
        validateDuplicateUsername(request.username());

        // email 중복 불가
        validateDuplicateEmail(request.email());

        String password = passwordEncoder.encode(request.password());
        memberRepository.save(Member.addOf(request, password));
    }

    public TokenDto signIn(SignInRequestDto request) {
        Member member = memberRepository.findByUsername(request.username())
                .orElseThrow(() -> new CustomException(MEMBER_UNAUTHORIZED));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new CustomException(MEMBER_INVALID_CREDENTIAL);
        }

        // 토큰 생성
        String accessToken = jwtUtil.createAccessToken(member.getUsername(), member.getRole(), member.getId());
        String refreshToken = jwtUtil.createRefreshToken(member.getId());
        return new TokenDto(accessToken, refreshToken);
    }

    public String refresh(String accessToken, String refreshToken) {
        //  Access Token 이 만료되었는지 확인(만료가 정상)
        try {
            //  Refresh Token 검증
            jwtUtil.validateToken(accessToken);

            //  ExpiredJwtException 이 아니면 아직 만료 기한이 남아 있는 것이므로 그대로 리턴
            log.info("Access Token is not expired.....................");

            return accessToken;
        } catch (ExpiredJwtException expiredJwtException) {
            try {
                //  Refresh 가 필요한 상황
                Claims claims = jwtUtil.validateToken(refreshToken);
                Member member = memberRepository.findById(claims.get("id", Long.class))
                        .orElseThrow(() -> new CustomException(MEMBER_UNAUTHORIZED));

                // access token 생성, refresh token 은 따로 관리하지 않아 재 생성 시 기존 토큰을 삭제할 수 없으므로
                // refresh token 재발급은 하지 않음. (추후 관리 가능한 방법으로..)
                return jwtUtil.createAccessToken(member.getUsername(), member.getRole(), member.getId());
            } catch (Exception e) {
                log.error(e.getMessage());
                throw new CustomException(REFRESH_TOKEN_NOT_AVAILABLE);
            }
        } catch (JwtException e) {
            log.error(e.getMessage());
            throw new CustomException(ACCESS_TOKEN_NOT_AVAILABLE);
        }
    }

    private void validateDuplicateEmail(String email) {
        // 단순 존재여부 확인은 exists 를 활용 하는 것이 더 낫다.
        if (memberRepository.existsByEmail(email)) {
            throw new CustomException(MEMBER_DUPLICATE_EMAIL);
        }
    }

    private void validateDuplicateUsername(String username) {
        if (memberRepository.existsByUsername(username)) {
            throw new CustomException(MEMBER_DUPLICATE_USERNAME);
        }
    }
}
