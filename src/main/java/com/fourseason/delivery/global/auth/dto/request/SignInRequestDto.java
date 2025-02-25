package com.fourseason.delivery.global.auth.dto.request;

public record SignInRequestDto(
        String username,
        String password
) {
}
