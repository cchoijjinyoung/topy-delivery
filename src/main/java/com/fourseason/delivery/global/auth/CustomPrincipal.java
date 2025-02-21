package com.fourseason.delivery.global.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.security.Principal;

@RequiredArgsConstructor
public class CustomPrincipal implements Principal {

    @Getter
    private final Long id;

    private final String username;

    @Override
    public String getName() {
        return username;
    }

}
