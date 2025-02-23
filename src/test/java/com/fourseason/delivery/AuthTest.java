package com.fourseason.delivery;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourseason.delivery.domain.member.entity.Role;
import com.fourseason.delivery.global.auth.JwtUtil;
import com.fourseason.delivery.global.auth.dto.request.SignInRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("로그인 후 JWT 발행")
    void jwtTest() throws Exception {
        SignInRequestDto request = new SignInRequestDto("admin", "1234");

        mvc.perform(post("/api/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(header().exists("Authorization"));
    }

    @Test
    @DisplayName("유효한 token 으로 보호된 엔드포인트 접근")
    void validToken() throws Exception {
        String token = jwtUtil.createAccessToken("username", Role.CUSTOMER, 1L); // 유효한 토큰 생성

        mvc.perform(get("/api/member")
                        .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk())
                .andExpect(content().string("member name: username id: 1"));
    }
}
