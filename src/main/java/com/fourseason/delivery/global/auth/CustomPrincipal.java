package com.fourseason.delivery.global.auth;

import lombok.RequiredArgsConstructor;

import java.security.Principal;

@RequiredArgsConstructor
public class CustomPrincipal implements Principal {

    private final String username;

    @Override
    public String getName() {
        return username;
    }
}
