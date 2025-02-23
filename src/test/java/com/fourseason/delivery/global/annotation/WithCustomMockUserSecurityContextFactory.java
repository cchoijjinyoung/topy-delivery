package com.fourseason.delivery.global.annotation;

import com.fourseason.delivery.global.auth.CustomPrincipal;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithCustomMockUserSecurityContextFactory implements WithSecurityContextFactory<WithCustomMockUser> {

    @Override
    public SecurityContext createSecurityContext(WithCustomMockUser member) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        CustomPrincipal principal = new CustomPrincipal(member.username(), member.id(), "CUSTOMER");
        Authentication authentication = new TestingAuthenticationToken(principal, "", "CUSTOMER");
        context.setAuthentication(authentication);
        return context;
    }
}
