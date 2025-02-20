package com.fourseason.delivery.domain.member.dto.request;

import com.fourseason.delivery.domain.member.entity.Role;

public record MemberRequestDto(String username,
                               String email,
                               String phoneNumber,
                               Role role) {
}
