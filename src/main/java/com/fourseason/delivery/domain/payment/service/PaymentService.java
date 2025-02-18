package com.fourseason.delivery.domain.payment.service;

import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.order.entity.Order;
import com.fourseason.delivery.domain.order.entity.OrderStatus;
import com.fourseason.delivery.domain.payment.dto.request.CreatePaymentRequestDto;
import com.fourseason.delivery.domain.payment.dto.response.PaymentResponseDto;
import com.fourseason.delivery.domain.payment.entity.Payment;
import com.fourseason.delivery.domain.payment.exception.PaymentErrorCode;
import com.fourseason.delivery.domain.payment.repository.PaymentRepository;
import com.fourseason.delivery.domain.payment.repository.PaymentRepositoryCustom;
import com.fourseason.delivery.domain.shop.entity.Shop;
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

    /**
     * 사용자 결제 전체 조회
     */
    @Transactional(readOnly = true)
    public PageResponseDto<PaymentResponseDto> findPaymentList(final PageRequestDto pageRequestDto, final Member member) {

        return paymentRepositoryCustom.findPaymentListByMemberWithPage(pageRequestDto, member);
    }

    /**
     * 사용자 결제 상세 조회
     */
    @Transactional(readOnly = true)
    public PaymentResponseDto getPayment(final UUID paymentId, final Member member) {
        Payment payment = checkPayment(paymentId, member);

        return PaymentResponseDto.of(payment);
    }

    /**
     * 결제 생성
     */
    @Transactional
    public URI registerPayment(final CreatePaymentRequestDto createPaymentRequestDto, final Member member) {
        // Todo: 임시 order 객체 생성 실제로는 order객체 조회, 확인 필요
        Order order = new Order(OrderStatus.PENDING, "test", 1, Shop.builder().build(), member);
        // Todo: pb사에 결제 승인처리, 결제 성공확인, 받은 객체에서 status 값 적용, 현재는 임시로 "DONE" 사용
        Payment newPayment = Payment.addOf(createPaymentRequestDto, "DONE", order, member);
        paymentRepository.save(newPayment);

        return ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newPayment.getId())
                .toUri();
    }

    /**
     * 결제 취소
     */
    @Transactional
    public URI cancelPayment(final UUID paymentId, final Member member) {
        Payment payment = checkPayment(paymentId, member);
        // Todo: pb사에 결제 취소처리, 취소 성공확인, 받은 객체에서 status 값 적용, 현재는 임시로 "CANCELED" 사용
        payment.cancelOf("CANCELED");

        return ServletUriComponentsBuilder
                .fromCurrentRequest()
                .build()
                .toUri();
    }

    /**
     * 결제 삭제
     */
    @Transactional
    public void deletePayment(final UUID paymentId, final Member member) {
        Payment payment = checkPayment(paymentId, member);

        payment.deleteOf(member.getUsername());
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
     * 에러처리
     */
    private Payment checkPayment(final UUID paymentId, final Member member) {
        Payment payment = paymentRepository.findByIdAndDeletedAtIsNotNull(paymentId)
                .orElseThrow(() -> new CustomException(PaymentErrorCode.PAYMENT_NOT_FOUND));
        if (!payment.getMember().getId().equals(member.getId())) {
            throw new CustomException(PaymentErrorCode.PAYMENT_NOT_UNAUTHORIZED);
        }

        return payment;
    }

    private Payment checkPayment(final UUID paymentId) {

        return paymentRepository.findByIdAndDeletedAtIsNotNull(paymentId)
                .orElseThrow(() -> new CustomException(PaymentErrorCode.PAYMENT_NOT_FOUND));
    }
}
