package com.fourseason.delivery.domain.payment.entity;

import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.order.entity.Order;
import com.fourseason.delivery.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "p_payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseTimeEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String paymentKey;

    @Column(nullable = false)
    private int paymentAmount;

    @Column(nullable = false)
    private String paymentMethod;

    @Column(nullable = false)
    private String paymentStatus;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Builder
    public Payment(String paymentKey, int paymentAmount, String paymentMethod, String paymentStatus, Order order, Member member) {
        this.paymentKey = paymentKey;
        this.paymentAmount = paymentAmount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.order = order;
        this.member = member;
    }
}
