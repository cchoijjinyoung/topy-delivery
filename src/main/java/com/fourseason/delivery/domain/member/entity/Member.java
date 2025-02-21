package com.fourseason.delivery.domain.member.entity;

import com.fourseason.delivery.domain.member.dto.request.MemberRequestDto;
import com.fourseason.delivery.global.auth.dto.SignUpRequestDto;
import com.fourseason.delivery.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "p_member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Builder
    public Member(String username, String email, String password, String phoneNumber, Role role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    public static Member addOf(SignUpRequestDto request, String password) {
        return Member.builder()
                .username(request.username())
                .email(request.email())
                .password(password)
                .phoneNumber(request.phoneNumber())
                .role(Role.CUSTOMER)
                .build();
    }

    public void updateOf(MemberRequestDto dto) {
        if (dto.username() != null) {
            this.username = dto.username();
        }
        if (dto.email() != null) {
            this.email = dto.email();
        }
        if (dto.phoneNumber() != null) {
            this.phoneNumber = dto.phoneNumber();
        }
        if (dto.role() != null) {
            this.role = dto.role();
        }
    }

    public void deleteOf(String deletedBy) {
        super.deleteOf(deletedBy);
    }
}
