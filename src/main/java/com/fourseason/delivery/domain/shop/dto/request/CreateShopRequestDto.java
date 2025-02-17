package com.fourseason.delivery.domain.shop.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record CreateShopRequestDto(
        @NotBlank(message = "가게 이름은 필수 입력 값입니다.")
        String name,

        @NotBlank(message = "가게 설명은 필수 입력 값입니다.")
        String description,

        @NotBlank(message = "가게 전화번호는 필수 입력 값입니다.")
        String tel,

        @NotBlank(message = "가게 주소는 필수 입력 값입니다.")
        String address,

        @NotBlank(message = "가게 상세 주소는 필수 입력 값입니다.")
        String detailAddress,

        @NotBlank(message = "가게 카테고리는 필수 입력 값입니다.")
        String category,

        List<MultipartFile> images
) {
}
