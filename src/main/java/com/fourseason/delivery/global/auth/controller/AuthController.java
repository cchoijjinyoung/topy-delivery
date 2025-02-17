package com.fourseason.delivery.global.auth.controller;

import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.global.auth.JwtUtil;
import com.fourseason.delivery.global.auth.dto.SignInRequestDto;
import com.fourseason.delivery.global.auth.dto.SignUpRequestDto;
import com.fourseason.delivery.global.auth.dto.TokenResponseDto;
import com.fourseason.delivery.global.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @PostMapping("/sign-up")
    public ResponseEntity<Void> signUp(
            @RequestBody SignUpRequestDto request,
            UriComponentsBuilder ucb
    ) {
        Member createdMember = authService.signUp(request);
        URI location = ucb.path("/api/sign-in").build().toUri();
        return ResponseEntity.created(location).build();
    }

    @PostMapping("/sign-in")
    public ResponseEntity<TokenResponseDto> signIn(
            @RequestBody SignInRequestDto request
    ) {
        TokenResponseDto response = authService.signIn(request);
        return ResponseEntity.ok()
                .header("Authorization", "Bearer" + response.accessToken())
                .header("X-Refresh-Token", response.refreshToken())
                .build();
    }
}

