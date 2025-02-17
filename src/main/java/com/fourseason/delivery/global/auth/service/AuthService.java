package com.fourseason.delivery.global.auth.service;

import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.member.repository.MemberRepository;
import com.fourseason.delivery.global.auth.JwtUtil;
import com.fourseason.delivery.global.auth.dto.SignInRequestDto;
import com.fourseason.delivery.global.auth.dto.SignUpRequestDto;
import com.fourseason.delivery.global.auth.dto.TokenResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public Member signUp(SignUpRequestDto signUpRequestDto) {
        // TODO: 가입 로직 구현
        return null;
    }

    public TokenResponseDto signIn(SignInRequestDto request) {
        Member member = memberRepository.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("Not Found Exception"));
        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new RuntimeException("Invalid Credentials");
        }

        // 토큰 생성
        String accessToken = jwtUtil.createAccessToken(member.getUsername(), member.getRole());
        String refreshToken = jwtUtil.createRefreshToken(member.getUsername());
        return new TokenResponseDto(accessToken, refreshToken);
    }
}
