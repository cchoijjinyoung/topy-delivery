package com.fourseason.delivery.domain.payment.entity;

import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.order.entity.Order;
import com.fourseason.delivery.domain.payment.dto.external.ExternalCancelPaymentDto;
import com.fourseason.delivery.domain.payment.dto.external.ExternalCancelPaymentDto.Cancel;
import com.fourseason.delivery.domain.payment.dto.external.ExternalPaymentDto;
import com.fourseason.delivery.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;
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

    private String cancelReason;

    @Column(nullable = false)
    private int balanceAmount;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Builder
    private Payment(final String paymentKey,
                   final int paymentAmount,
                   final String paymentMethod,
                   final String paymentStatus,
                   final int balanceAmount,
                   final Order order,
                   final Member member) {
        this.paymentKey = paymentKey;
        this.paymentAmount = paymentAmount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.balanceAmount = balanceAmount;
        this.order = order;
        this.member = member;
    }

    public static Payment addOf(final ExternalPaymentDto dto, final Order order, final Member member) {
        return Payment.builder()
                .paymentKey(dto.paymentKey())
                .paymentAmount(dto.amount())
                .paymentMethod(dto.method())
                .paymentStatus(dto.status())
                .balanceAmount(dto.amount())
                .order(order)
                .member(member)
                .build();
    }

    public void cancelOf(final ExternalCancelPaymentDto dto) {
        this.balanceAmount = dto.balanceAmount();
        this.paymentStatus = dto.status();
        List<Cancel> cancels = dto.cancels();
        this.cancelReason = cancels.get(cancels.size()-1).cancelReason();
    }

    public void deleteOf(final String deletedBy) {
        super.deleteOf(deletedBy);
    }
}
