package com.fourseason.delivery.domain.member.entity;

import com.fourseason.delivery.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "p_member_address")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address extends BaseTimeEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String detailAddress;

    @OneToOne
    @JoinColumn(name = "member_id")
    private Member member;
}
