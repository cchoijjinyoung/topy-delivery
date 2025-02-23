package com.fourseason.delivery.global.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.security.Principal;

@RequiredArgsConstructor
public class CustomPrincipal implements Principal {

    private final String username;

    @Getter
    private final Long id;

    @Getter
    private final String role;

    @Override
    public String getName() {
        return username;
    }
}
