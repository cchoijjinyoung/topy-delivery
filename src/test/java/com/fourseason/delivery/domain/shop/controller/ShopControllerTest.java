package com.fourseason.delivery.domain.shop.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourseason.delivery.domain.shop.dto.request.CreateShopRequestDto;
import com.fourseason.delivery.domain.shop.dto.response.ShopResponseDto;
import com.fourseason.delivery.domain.shop.exception.ShopErrorCode;
import com.fourseason.delivery.domain.shop.service.ShopService;
import com.fourseason.delivery.global.annotation.WithCustomMockUser;
import com.fourseason.delivery.global.dto.PageResponseDto;
import com.fourseason.delivery.global.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ShopController.class)
@ExtendWith(MockitoExtension.class)
@WithCustomMockUser
class ShopControllerTest {

    @MockitoBean
    private ShopService shopService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private final UUID shopId = UUID.fromString("e3c7a4b4-5aaf-4c7d-bdff-c64dbf7ef6f9");

    @Nested
    @DisplayName("가게 목록 조회")
    class 가게_목록_조회 {

        @Test
        @DisplayName("성공")
        void 성공() throws Exception {
            // given
            ShopResponseDto responseDto = getShopListResDto();
            PageResponseDto<ShopResponseDto> pageResponseDto = new PageResponseDto<>(List.of(responseDto), 1);

            given(shopService.getShopList(any())).willReturn(pageResponseDto);

            // when & then
            ResultActions actions = mockMvc.perform(get("/api/shops")
                .param("page", "1")
                .param("size", "10"));

            actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].name").value("00피자"))
                .andExpect(jsonPath("$.totalElements").value(1));
        }

        private ShopResponseDto getShopListResDto() {
            return ShopResponseDto.builder()
                .id(shopId.toString())
                .name("00피자")
                .description("맛있는 피자")
                .tel("010-1234-5678")
                .address("서울시 강남구")
                .detailAddress("2층")
                .images(List.of("image1.jpg", "image2.jpg"))
                .build();
        }
    }

    @Nested
    @DisplayName("가게 상세 조회")
    class 가게_상세_조회 {

        @Test
        @DisplayName("성공")
        void 성공() throws Exception {

            // given
            ShopResponseDto responseDto = getShopListResDto();

            given(shopService.getShop(shopId)).willReturn(responseDto);

            // when & then
            ResultActions actions = mockMvc.perform(get("/api/shops/{shopId}", shopId));

            actions.andExpect(status().isOk())  // 응답 상태 코드가 200 OK인지 확인
                .andExpect(jsonPath("$.name").value("00피자"))
                .andExpect(jsonPath("$.description").value("맛있는 피자"))
                .andExpect(jsonPath("$.tel").value("010-1234-5678"))
                .andExpect(jsonPath("$.address").value("서울시 강남구"));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 가게 ID")
        void 실패_존재하지_않는_가게_ID() throws Exception {
            // given
            given(shopService.getShop(shopId)).willThrow(new CustomException(ShopErrorCode.SHOP_NOT_FOUND));

            // when & then
            ResultActions actions = mockMvc.perform(get("/api/shops/{shopId}", shopId));

            actions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ShopErrorCode.SHOP_NOT_FOUND.getMessage()));
        }

        private ShopResponseDto getShopListResDto() {
            return ShopResponseDto.builder()
                .id(shopId.toString())
                .name("00피자")
                .description("맛있는 피자")
                .tel("010-1234-5678")
                .address("서울시 강남구")
                .detailAddress("2층")
                .images(List.of("image1.jpg", "image2.jpg"))
                .build();
        }
    }

    @Nested
    @DisplayName("가게 등록")
    class 가게_등록 {

        @Test
        @DisplayName("성공")
        void 성공() throws Exception {
            // given
            CreateShopRequestDto requestDto = getCreateShopRequestDto();

            MockMultipartFile requestDtoFile = new MockMultipartFile(
                "createShopRequestDto",
                null,
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsString(requestDto).getBytes()
            );
            MockMultipartFile imageFile = new MockMultipartFile(
                "images",
                "image.jpg",
                "image/jpeg",
                "dummy image content".getBytes()
            );

            // when & then
            ResultActions actions = mockMvc.perform(multipart("/api/shops")
                    .file(requestDtoFile)
                    .file(imageFile)
                    .with(csrf().asHeader())
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                );

            actions
                .andExpect(status().isOk())
                .andExpect(content().string(""));

            then(shopService).should().registerShop(any(), any(), any());
        }

        private CreateShopRequestDto getCreateShopRequestDto() {
            return CreateShopRequestDto.builder()
                .name("00피자")
                .description("맛있는 피자")
                .tel("010-1234-5678")
                .address("서울시 강남구")
                .detailAddress("2층")
                .category("피자")
                .build();
        }
    }

    @Nested
    @DisplayName("가게 삭제")
    class 가게_삭제 {

        @Test
        @DisplayName("성공")
        void 성공() throws Exception {

            // when & then
            ResultActions actions = mockMvc.perform(delete("/api/shops/{shopId}", shopId)
                .with(csrf().asHeader()));

            actions
                .andExpect(status().isOk())
                .andExpect(content().string(""));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 ID")
        void 실패_존재하지_않는_ID() throws Exception {

            // given
            willThrow(new CustomException(ShopErrorCode.SHOP_NOT_FOUND)).given(shopService).deleteShop(shopId, 1L);

            // when & then
            ResultActions actions = mockMvc.perform(delete("/api/shops/{shopId}", shopId)
                .with(csrf().asHeader()));

            actions.andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(ShopErrorCode.SHOP_NOT_FOUND.getMessage()));
        }
    }
}