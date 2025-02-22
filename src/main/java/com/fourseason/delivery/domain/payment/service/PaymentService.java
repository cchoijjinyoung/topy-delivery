package com.fourseason.delivery.domain.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourseason.delivery.domain.member.MemberErrorCode;
import com.fourseason.delivery.domain.member.entity.Member;
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
import com.fourseason.delivery.global.dto.PageRequestDto;
import com.fourseason.delivery.global.dto.PageResponseDto;
import com.fourseason.delivery.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import com.fourseason.delivery.domain.payment.dto.external.ExternalCancelPaymentDto.Cancel;

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
    public PageResponseDto<PaymentResponseDto> findPaymentList(final PageRequestDto pageRequestDto, final String username) {

        return paymentRepositoryCustom.findPaymentListByMemberWithPage(pageRequestDto, checkMember(username));
    }

    /**
     * 사용자 결제 상세 조회
     */
    @Transactional(readOnly = true)
    public PaymentResponseDto getPayment(final UUID paymentId, final String username) {
        Payment payment = checkPayment(paymentId, checkMember(username));

        return PaymentResponseDto.of(payment);
    }

    /**
     * 결제 승인 과정
     * 1. 현재 order가 존재하는지 확인
     * 2. 확인된 orderId를 담아서 결제 승인처리 (paymentRestService 참조)
     * 3. 성공 or 실패 처리에 맞게 결제 정보 db에 저장
     * 4. response로 돌려줌
     *
     */
    @Transactional
    public URI registerPayment(final String paymentResult) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ExternalPaymentDto externalPaymentDto = objectMapper.readValue(paymentResult, ExternalPaymentDto.class);
            Order order = orderRepository.findById(externalPaymentDto.orderId())
                    .orElseThrow(() ->
                            new CustomException(OrderErrorCode.ORDER_NOT_FOUND)
                    );
            Payment payment = Payment.addOf(externalPaymentDto, order, order.getMember());
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
     * 결제 취소 추가 구현 필요
     */
    @Transactional
    public URI cancelPayment(final UUID paymentId, final String paymentResult) {
        try {
//            Payment payment = checkPayment(paymentId, checkMember(username));
//            Payment payment = paymentRepository.findByIdAndDeletedAtIsNotNull(paymentId).orElseThrow(
//                    () -> new CustomException(PaymentErrorCode.PAYMENT_NOT_FOUND));

            ObjectMapper objectMapper = new ObjectMapper();
            ExternalCancelPaymentDto externalCancelPaymentDto = objectMapper.readValue(paymentResult, ExternalCancelPaymentDto.class);
            int balenceamount = externalCancelPaymentDto.balanceAmount();
            List<Cancel> cancels = externalCancelPaymentDto.cancels();
            String cancelReason = cancels.get(cancels.size() - 1).cancelReason();

            System.out.println(balenceamount);
            System.out.println(cancelReason);

            return null;

//            payment.cancelOf(externalCancelPaymentDto);
//
//            return ServletUriComponentsBuilder
//                    .fromCurrentRequest()
//                    .path("/{id}")
//                    .buildAndExpand(payment.getId())
//                    .toUri();
        } catch (JsonProcessingException e) {
            throw new CustomException(PaymentErrorCode.PAYMENT_MAPPING_FAIL);
        }
    }

    /**
     * 결제 삭제
     */
    @Transactional
    public void deletePayment(final UUID paymentId, final String username) {
        Payment payment = checkPayment(paymentId, checkMember(username));

        payment.deleteOf(username);
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

    private Member checkMember(final String username) {
        Member member = memberRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));
        return member;
    }
}
