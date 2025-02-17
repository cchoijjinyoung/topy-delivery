package com.fourseason.delivery.global.auth.dto;

public record TokenResponseDto(
        String accessToken,
        String refreshToken
) {
}
