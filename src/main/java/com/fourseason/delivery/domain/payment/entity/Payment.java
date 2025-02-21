package com.fourseason.delivery.domain.payment.entity;

import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.order.entity.Order;
import com.fourseason.delivery.domain.payment.dto.external.ExternalPaymentDto;
import com.fourseason.delivery.domain.payment.dto.request.CreatePaymentRequestDto;
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
@Table(name = "p_payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseTimeEntity {

    @Id
    @UuidGenerator
    private UUID id;

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
    public Payment(final String paymentKey,
                   final int paymentAmount,
                   final String paymentMethod,
                   final String paymentStatus,
                   final Order order,
                   final Member member) {
        this.paymentKey = paymentKey;
        this.paymentAmount = paymentAmount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.order = order;
        this.member = member;
    }

    /**
     * TODO: requestDto에서 만들것이 아니라 결제결과에서 오는 payment객체를 받아서 결제 정보를 생성하는쪽이 좋을것 같다.
     */
    public static Payment addOf(final ExternalPaymentDto dto, final Order order, final Member member) {
        return Payment.builder()
                .paymentKey(dto.paymentKey())
                .paymentAmount(dto.amount())
                .paymentMethod(dto.method())
                .paymentStatus(dto.status())
                .order(order)
                .member(member)
                .build();
    }

    //updateOf cancelOf 고민
    public void cancelOf(final String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void deleteOf(final String deletedBy) {
        super.deleteOf(deletedBy);
    }
}
