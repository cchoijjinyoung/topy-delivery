package com.fourseason.delivery.domain.order.entity;

import com.fourseason.delivery.domain.member.entity.Role;

public enum Status {
    PENDING,
    CANCEL,
    COMPLETED;

    public static Status of(String status) {
        return valueOf(status);
    }
}
