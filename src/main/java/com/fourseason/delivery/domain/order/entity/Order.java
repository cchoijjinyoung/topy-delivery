package com.fourseason.delivery.domain.order.entity;

import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.shop.entity.Shop;
import com.fourseason.delivery.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "p_order")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseTimeEntity {
    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    private String instruction;

    @Column(nullable = false)
    private int totalAmount;

    @ManyToOne
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Builder
    public Order(Status status, String instruction, int totalAmount, Shop shop, Member member) {
        this.status = status;
        this.instruction = instruction;
        this.totalAmount = totalAmount;
        this.shop = shop;
        this.member = member;
    }

    @PrePersist
    public void prePersist() {
        this.status = this.status == null ? Status.PENDING : this.status;
    }
}
