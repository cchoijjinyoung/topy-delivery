package com.fourseason.delivery.domain.member.dto.response;

import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.member.entity.Role;
import lombok.Builder;

@Builder
public record MemberResponseDto(
        String username,
        String email,
        String phoneNumber,
        Role role
) {
    public static MemberResponseDto of(Member member) {
        return MemberResponseDto.builder()
                .username(member.getUsername())
                .email(member.getEmail())
                .phoneNumber(member.getPhoneNumber())
                .role(member.getRole())
                .build();
    }
}
