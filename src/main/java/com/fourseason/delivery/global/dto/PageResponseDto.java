package com.fourseason.delivery.global.dto;

import java.util.List;

public record PageResponseDto<T>(
        List<T> result,
        long totalElements
) {
}
