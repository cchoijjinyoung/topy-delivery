package com.fourseason.delivery.global.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourseason.delivery.global.auth.dto.request.SignUpRequestDto;
import com.fourseason.delivery.global.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    private SignUpRequestDto alice, bob;

    @Autowired
    private MockMvcTester mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @BeforeEach
    void setUp() {
        // 유효 값
        alice = new SignUpRequestDto(
                "alice",
                "alice@gmail.com",
                "valid1234%",
                "010-1234-5678",
                "CUSTOMER"
        );

        // 유효하지 않은 값
        bob = new SignUpRequestDto(
                "bob",
                "bob@.com",
                "1234",
                "0000-1111-2222",
                "CUSTOMER"
        );
    }

    @Test
    void testMockBeanInjection() {
        assertNotNull(authService, "빈 주입 실패");
    }

    @Test
    @WithMockUser
    @DisplayName("sign-up: 유효한 입력 값일 때")
    void validSignUpRequest() throws Exception {
        //  given
        willDoNothing().given(authService).signUp(any(SignUpRequestDto.class));

        //  when & then
        assertThat(mvc.post().uri("/api/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(alice))
                .with(csrf())
        ).hasStatus(HttpStatus.CREATED)
                .hasHeader("Location",  "/api/sign-in");
    }

    @Test
    @WithMockUser
    @DisplayName("sign-up: 유효하지 않은 입력 값")
    void invalidSignUpRequest() throws Exception {
        willDoNothing().given(authService).signUp(any(SignUpRequestDto.class));

        //  when & then
        assertThat(mvc.post().uri("/api/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bob))
                .with(csrf())
        ).hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .asString()
                .contains("이메일 형식에 맞게 입력해 주세요.")
                .contains("Username 은 소문자와 숫자로 4~10자 사이여야 합니다.")
                .contains("password 는 대소문자, 숫자, 특수문자를 포함한 8~15자 사이여야 합니다.")
                .contains("전화번호 형식에 맞게 입력해 주세요.");
    }
}