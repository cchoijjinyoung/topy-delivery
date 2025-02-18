package com.fourseason.delivery.global.auth.dto;

import jakarta.validation.constraints.*;

public record SignUpRequestDto (
        @NotBlank(message = "이름은 필수 항목 입니다.")
        String username,

        @Email(message = "이메일 형식에 맞게 입력해 주세요.")
        String email,

        @Size(min = 4, message = "비밀번호는 최소 4자 이상이어야 합니다.")
        String password,

        @Pattern(regexp = "^\\d{3}-\\d{3,4}-\\d{4}$", message = "전화번호 형식에 맞게 입력해 주세요.")
        String phoneNumber
) {
}
