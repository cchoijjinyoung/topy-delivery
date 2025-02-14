package com.fourseason.delivery.domain.menu.entity;

public enum MenuStatus {

    SHOW,
    HIDE,
    SOLD_OUT;

    public static MenuStatus of(String status) {
        return valueOf(status);
    }
}
