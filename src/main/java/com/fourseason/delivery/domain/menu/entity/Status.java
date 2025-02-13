package com.fourseason.delivery.domain.menu.entity;

public enum Status {

    SHOW,
    HIDE,
    SOLD_OUT;

    public static Status of(String status) {
        return valueOf(status);
    }
}
