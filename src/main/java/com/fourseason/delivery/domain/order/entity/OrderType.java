package com.fourseason.delivery.domain.order.entity;

public enum OrderType {
    ONLINE,
    OFFLINE;

    public static OrderType of(String status) {
        return valueOf(status);
    }
}
