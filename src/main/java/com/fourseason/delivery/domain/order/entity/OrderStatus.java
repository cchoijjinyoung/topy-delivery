package com.fourseason.delivery.domain.order.entity;

public enum OrderStatus {
    PENDING,
    CANCEL,
    COMPLETED;

    public static OrderStatus of(String status) {
        return valueOf(status);
    }
}
