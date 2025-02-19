package com.fourseason.delivery.domain.member.entity;

import com.fourseason.delivery.domain.member.dto.request.AddressRequestDto;
import com.fourseason.delivery.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@Table(name = "p_member_address")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address extends BaseTimeEntity {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String detailAddress;

    @OneToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Builder
    public Address(String address, String detailAddress, Member member) {
        this.address = detailAddress;
        this.detailAddress = detailAddress;
        this.member = member;
    }

    public static Address addOf(AddressRequestDto dto, Member member) {
        return Address.builder()
                .address(dto.address())
                .detailAddress(dto.detailAddress())
                .member(member)
                .build();
    }

    public void updateOf(AddressRequestDto addressRequestDto) {
        this.address = addressRequestDto.address();
        this.detailAddress = addressRequestDto.detailAddress();
    }
}
