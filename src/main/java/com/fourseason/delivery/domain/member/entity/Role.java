package com.fourseason.delivery.domain.member.entity;

public enum Role {
    MASTER,
    OWNER,
    MANAGER,
    CUSTOMER;

    public static Role of(String role) {
        return valueOf(role);
    }
}
