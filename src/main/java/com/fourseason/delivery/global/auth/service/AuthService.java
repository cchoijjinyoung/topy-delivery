package com.fourseason.delivery.global.auth.service;

import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.member.repository.MemberRepository;
import com.fourseason.delivery.global.auth.JwtUtil;
import com.fourseason.delivery.global.auth.dto.SignInRequestDto;
import com.fourseason.delivery.global.auth.dto.SignUpRequestDto;
import com.fourseason.delivery.global.auth.dto.TokenDto;
import com.fourseason.delivery.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.fourseason.delivery.domain.member.exception.MemberErrorCode.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public void signUp(SignUpRequestDto request) {
        // email 중복 불가
        validateDuplicateEmail(request.email());

        String password = passwordEncoder.encode(request.password());
        memberRepository.save(Member.addOf(request, password));
    }

    public TokenDto signIn(SignInRequestDto request) {
        Member member = memberRepository.findByUsername(request.username())
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new CustomException(MEMBER_INVALID_CREDENTIAL);
        }

        // 토큰 생성
        String accessToken = jwtUtil.createAccessToken(member.getUsername(), member.getRole());
        String refreshToken = jwtUtil.createRefreshToken(member.getUsername());
        return new TokenDto(accessToken, refreshToken);
    }

    private void validateDuplicateEmail(String email) {
        memberRepository.findByEmail(email)
                .ifPresent(member-> {
                    throw new CustomException(MEMBER_DUPLICATE_EMAIL);
                });
    }
}
