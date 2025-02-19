package com.fourseason.delivery.global.auth.dto;

public record SignInRequestDto(
        String username,
        String password
) {
}
