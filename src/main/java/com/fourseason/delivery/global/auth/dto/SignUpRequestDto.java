package com.fourseason.delivery.global.auth.dto;

public record SignUpRequestDto (
        String username,
        String nickname,
        String email,
        String password,
        String phone_number
) {
}
