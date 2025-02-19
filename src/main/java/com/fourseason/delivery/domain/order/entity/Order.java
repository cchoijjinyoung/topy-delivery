package com.fourseason.delivery.domain.order.entity;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.FetchType.*;

import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.order.dto.request.CreateOrderRequestDto;
import com.fourseason.delivery.domain.shop.entity.Shop;
import com.fourseason.delivery.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;


@Entity
@Getter
@Table(name = "p_order")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Order extends BaseTimeEntity {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(fetch = LAZY, cascade = PERSIST)
    private List<OrderMenu> orderMenuList;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;

    @Column(nullable = false)
    private String address;

    private String instruction;

    @Column(nullable = false)
    private int totalPrice;

    @Builder
    private Order(Shop shop, Member member, List<OrderMenu> orderMenuList,
        OrderStatus orderStatus, String address, String instruction, int totalPrice) {
        this.shop = shop;
        this.member = member;
        this.orderMenuList = orderMenuList;
        this.orderStatus = orderStatus;
        this.address = address;
        this.instruction = instruction;
        this.totalPrice = totalPrice;
    }

    public static Order addOf(
        CreateOrderRequestDto dto,
        Shop shop,
        Member member,
        List<OrderMenu> orderMenuList,
        int totalPrice) {
        return Order.builder()
            .shop(shop)
            .member(member)
            .orderStatus(OrderStatus.PENDING)
            .address(dto.address())
            .instruction(dto.instruction())
            .totalPrice(totalPrice)
            .orderMenuList(orderMenuList)
            .build();
    }
}
