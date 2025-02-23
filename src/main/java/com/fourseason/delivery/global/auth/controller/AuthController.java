package com.fourseason.delivery.global.auth.controller;

import com.fourseason.delivery.domain.member.entity.Role;
import com.fourseason.delivery.global.auth.CustomPrincipal;
import com.fourseason.delivery.global.auth.dto.TokenDto;
import com.fourseason.delivery.global.auth.dto.request.SignInRequestDto;
import com.fourseason.delivery.global.auth.dto.request.SignUpRequestDto;
import com.fourseason.delivery.global.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
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
        SignUpRequestDto newMember = new SignUpRequestDto(
                request.username(),
                request.email(),
                request.password(),
                request.phoneNumber(),
                Role.CUSTOMER.toString()
        );
        authService.signUp(newMember);

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

    // refresh token 요청 경로, filter 에서 체크되지 않도록 /api/sign
    @PostMapping("/sign-refresh")
    public ResponseEntity<Void> refreshToken(
            @RequestHeader("Authorization") String accessToken,
            @RequestHeader("X-Refresh-Token") String refreshToken
    ) {
        if (accessToken == null || !accessToken.startsWith("Bearer ") || refreshToken == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        String newAccessToken = authService.refresh(accessToken.substring(7), refreshToken);

        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + newAccessToken)
                .header("X-Refresh-Token", refreshToken)
                .build();
    }

    @Secured("ROLE_MASTER")
    @PostMapping("/admin/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    public String adminSignUp(
            @RequestBody @Valid SignUpRequestDto request
    ) {
        authService.signUp(request);
        return request.role() + " member 생셩.";
    }

    //    권한 테스트용
    @PreAuthorize("hasRole('ROLE_MASTER')")
    @GetMapping("/admin")
    public String admin(@AuthenticationPrincipal CustomPrincipal customPrincipal) {
        return "admin name: " + customPrincipal.getName() +
                " id: " + customPrincipal.getId() +
                " role: " + customPrincipal.getRole();
    }

    @GetMapping("/member")
    public String member(@AuthenticationPrincipal CustomPrincipal customPrincipal) {
        return "member name: " + customPrincipal.getName() +
                " id: " + customPrincipal.getId() +
                " role: " + customPrincipal.getRole();
    }
}

