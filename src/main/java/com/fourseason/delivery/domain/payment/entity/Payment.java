package com.fourseason.delivery.domain.payment.entity;

import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.order.entity.Order;
import com.fourseason.delivery.domain.payment.dto.requestDto.CreatePaymentRequestDto;
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
    public Payment(String paymentKey, int paymentAmount, String paymentMethod, String paymentStatus, Order order, Member member) {
        this.paymentKey = paymentKey;
        this.paymentAmount = paymentAmount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.order = order;
        this.member = member;
    }

    public static Payment addOf(CreatePaymentRequestDto dto, String paymentStatus, Order order, Member member) {
        return Payment.builder()
                .paymentKey(dto.paymentKey())
                .paymentAmount(dto.paymentAmount())
                .paymentMethod(dto.paymentMethod())
                // Todo: 결제 승인 DONE, 결제 취소 CANCELED, 결제 실패 ABORTED, EXPIRED
                // 외부 api 서비스를 통해 값을 받다보니 변동성이 있어 string으로 사용
                .paymentStatus(paymentStatus)
                .order(order)
                .member(member)
                .build();
    }

    //updateOf cancelOf 고민
    public void cancelOf(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void deleteOf(String deletedBy) {
        super.deleteOf(deletedBy);
    }
}
