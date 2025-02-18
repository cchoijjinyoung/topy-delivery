package com.fourseason.delivery.domain.review.dto.request;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record ReviewRequestDto(
        String content,

        @NotNull(message = "평점은 필수 입력 값입니다.")
        @PositiveOrZero(message = "평점은 0 이상이어야 합니다.")
        int rating,

        List<MultipartFile> images
) {
}
