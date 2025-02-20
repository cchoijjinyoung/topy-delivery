package com.fourseason.delivery.global.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SignUpRequestDto (
        @NotBlank(message = "username 은 필수 항목 입니다.")
        @Pattern(regexp = "^[a-z0-9]{4,10}$", message = "Username 은 소문자와 숫자로 4~10자 사이여야 합니다.")
        String username,

        @Email(message = "이메일 형식에 맞게 입력해 주세요.")
        String email,

        @NotBlank(message = "password 는 필수 항목 입니다.")
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}|:\"<>?\\[\\]\\\\/]).{8,15}$",
                message = "password 는 대소문자, 숫자, 특수문자를 포함한 8~15자 사이여야 합니다.")
        String password,

        @Pattern(regexp = "^\\d{3}-\\d{3,4}-\\d{4}$", message = "전화번호 형식에 맞게 입력해 주세요.")
        String phoneNumber,

        // 직접 받을 수 있는 항목을 넣었는데 enum 값 변경에 유연하지 않다...
        @Pattern(regexp = "CUSTOMER|MANAGER|OWNER|MASTER", message = "권한 이름을 확인해 주세요.")
        String role
) {
}
