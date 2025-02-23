package com.fourseason.delivery.domain.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.member.exception.MemberErrorCode;
import com.fourseason.delivery.domain.member.repository.MemberRepository;
import com.fourseason.delivery.domain.order.entity.Order;
import com.fourseason.delivery.domain.order.exception.OrderErrorCode;
import com.fourseason.delivery.domain.order.repository.OrderRepository;
import com.fourseason.delivery.domain.payment.dto.external.ExternalCancelPaymentDto;
import com.fourseason.delivery.domain.payment.dto.external.ExternalPaymentDto;
import com.fourseason.delivery.domain.payment.dto.response.PaymentResponseDto;
import com.fourseason.delivery.domain.payment.entity.Payment;
import com.fourseason.delivery.domain.payment.exception.PaymentErrorCode;
import com.fourseason.delivery.domain.payment.repository.PaymentRepository;
import com.fourseason.delivery.domain.payment.repository.PaymentRepositoryCustom;
import com.fourseason.delivery.global.auth.CustomPrincipal;
import com.fourseason.delivery.global.dto.PageRequestDto;
import com.fourseason.delivery.global.dto.PageResponseDto;
import com.fourseason.delivery.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentRepositoryCustom paymentRepositoryCustom;
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;

    /**
     * 사용자 결제 전체 조회
     */
    @Transactional(readOnly = true)
    public PageResponseDto<PaymentResponseDto> findPaymentList(final PageRequestDto pageRequestDto, final CustomPrincipal customPrincipal) {

        return paymentRepositoryCustom.findPaymentListByMemberWithPage(pageRequestDto, checkMember(customPrincipal.getId()));
    }

    /**
     * 사용자 결제 상세 조회
     */
    @Transactional(readOnly = true)
    public PaymentResponseDto getPayment(final UUID paymentId, final CustomPrincipal customPrincipal) {
        Payment payment = checkPayment(paymentId, checkMember(customPrincipal.getId()));

        return PaymentResponseDto.of(payment);
    }

    /**
     * 결제 등록
     */
    @Transactional
    public URI registerPayment(final String paymentResult, Long memberId) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ExternalPaymentDto externalPaymentDto = objectMapper.readValue(paymentResult, ExternalPaymentDto.class);

            Member member = checkMember(memberId);
            Order order = checkOrder(externalPaymentDto.orderId(), member);

            Payment payment = Payment.addOf(externalPaymentDto, order, member);
            paymentRepository.save(payment);

            return ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(payment.getId())
                    .toUri();
        } catch (JsonProcessingException e) {
            throw new CustomException(PaymentErrorCode.PAYMENT_MAPPING_FAIL);
        }
    }

    /**
     * 결제 취소
     */
    @Transactional
    public URI cancelPayment(final UUID paymentId, final String paymentResult, CustomPrincipal customPrincipal) {
        try {
            Payment payment = checkPayment(paymentId, checkMember(customPrincipal.getId()));

            ObjectMapper objectMapper = new ObjectMapper();
            ExternalCancelPaymentDto externalCancelPaymentDto = objectMapper.readValue(paymentResult, ExternalCancelPaymentDto.class);

            payment.cancelOf(externalCancelPaymentDto);

            return ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(payment.getId())
                    .toUri();
        } catch (JsonProcessingException e) {
            throw new CustomException(PaymentErrorCode.PAYMENT_MAPPING_FAIL);
        }
    }


    /**
     * 관리자 결제 전체 조회
     */
    @Transactional(readOnly = true)
    public PageResponseDto<PaymentResponseDto> findPaymentList(final PageRequestDto pageRequestDto) {

        return paymentRepositoryCustom.findPaymentListWithPage(pageRequestDto);
    }

    /**
     * 관리자 결제 상세 조회
     */
    @Transactional(readOnly = true)
    public PaymentResponseDto getPayment(final UUID paymentId) {
        Payment payment = checkPayment(paymentId);

        return PaymentResponseDto.of(payment);
    }

    /**
     * 결제 삭제
     */
    @Transactional
    public void deletePayment(final UUID paymentId, final CustomPrincipal customPrincipal) {
        Payment payment = checkPayment(paymentId);

        payment.deleteOf(customPrincipal.getName());
    }

    /**
     * 검증 및 에러처리
     */
    private Order checkOrder(final UUID orderId, final Member member) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new CustomException(OrderErrorCode.ORDER_NOT_FOUND));
        if (!order.getMember().getId().equals(member.getId())) {
            throw new CustomException(OrderErrorCode.NOT_ORDERED_BY_CUSTOMER);
            // NOT_ORDERED_BY_CUSTOMER forbiden이 맞지 않을까용?
        }

        return order;
    }

    private Payment checkPayment(final UUID paymentId, final Member member) {
        Payment payment = paymentRepository.findByIdAndDeletedAtIsNull(paymentId)
                .orElseThrow(() -> new CustomException(PaymentErrorCode.PAYMENT_NOT_FOUND));
        if (!payment.getMember().getId().equals(member.getId())) {
            throw new CustomException(PaymentErrorCode.PAYMENT_FORBIDDEN);
        }

        return payment;
    }

    private Payment checkPayment(final UUID paymentId) {

        return paymentRepository.findByIdAndDeletedAtIsNull(paymentId)
                .orElseThrow(() -> new CustomException(PaymentErrorCode.PAYMENT_NOT_FOUND));
    }

    private Member checkMember(final Long memberId) {
        Member member = memberRepository.findByIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));
        return member;
    }
}
