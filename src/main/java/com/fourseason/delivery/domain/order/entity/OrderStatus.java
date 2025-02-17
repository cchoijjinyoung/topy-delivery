package com.fourseason.delivery.domain.order.entity;

public enum OrderStatus {
    PENDING,
    ACCEPTED,
    DELIVERING,
    COMPLETED,
    CANCELED;

    public static OrderStatus of(String status) {
        return valueOf(status);
    }
}
