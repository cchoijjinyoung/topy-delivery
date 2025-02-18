package com.fourseason.delivery.global.auth.dto;

public record TokenDto(
        String accessToken,
        String refreshToken
) {
}
