package com.fourseason.delivery.global.auth.controller;

import com.fourseason.delivery.global.auth.CustomPrincipal;
import com.fourseason.delivery.global.auth.dto.request.SignInRequestDto;
import com.fourseason.delivery.global.auth.dto.request.SignUpRequestDto;
import com.fourseason.delivery.global.auth.dto.TokenDto;
import com.fourseason.delivery.global.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sign-up")
    public ResponseEntity<Void> signUp(
            @RequestBody @Valid SignUpRequestDto request
    ) {
        authService.signUp(request);
        URI location = UriComponentsBuilder.newInstance()
                .path("/api/sign-in")
                .build()
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @PostMapping("/sign-in")
    public ResponseEntity<Void> signIn(
            @RequestBody SignInRequestDto request
    ) {
        TokenDto response = authService.signIn(request);
        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + response.accessToken())
                .header("X-Refresh-Token", response.refreshToken())
                .build();
    }

    //    권한 테스트용
    @Secured("ROLE_MASTER")
    @GetMapping("/admin")
    public String admin(@AuthenticationPrincipal CustomPrincipal customPrincipal) {
        return "admin name: " + customPrincipal.getName();
    }

    @GetMapping("/member")
    public String member(@AuthenticationPrincipal CustomPrincipal customPrincipal) {
        return "member name: " + customPrincipal.getName();
    }
}

