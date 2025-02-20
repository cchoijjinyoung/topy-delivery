package com.fourseason.delivery.domain.image.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum S3Folder {
    SHOP("shop"),
    MENU("menu"),
    REVIEW("review");

    private final String folderName;
}
